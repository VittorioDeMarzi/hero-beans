package techcourse.herobeans.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.dto.CheckoutStartRequest
import techcourse.herobeans.dto.CheckoutStartResponse
import techcourse.herobeans.dto.FinalizePaymentRequest
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.PaymentError
import techcourse.herobeans.dto.PaymentErrorCode
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.dto.PaymentResult
import techcourse.herobeans.entity.Order
import techcourse.herobeans.enums.OrderStatus
import techcourse.herobeans.exception.InvalidCouponException
import techcourse.herobeans.exception.OrderAlreadyTerminatedException
import techcourse.herobeans.exception.OrderNotProcessableException
import techcourse.herobeans.exception.PaymentException
import techcourse.herobeans.exception.PaymentStatusNotSuccessException
import techcourse.herobeans.exception.StripeClientException
import techcourse.herobeans.exception.StripeProcessingException
import techcourse.herobeans.exception.StripeServerException
import techcourse.herobeans.mapper.AddressMapper.toDto
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

@Service
class CheckoutService(
    private val orderService: OrderService,
    private val paymentService: PaymentService,
    private val cartService: CartService,
    private val couponService: CouponService,
    private val addressService: AddressService,
) {
    @Transactional(
        rollbackFor = [Exception::class],
        timeout = 30,
    )
    fun startCheckout(
        memberDto: MemberDto,
        request: CheckoutStartRequest,
    ): CheckoutStartResponse {
        val address = addressService.findMemberAddress(addressId = request.addressId, memberId = memberDto.id)
        log.info { "checkout.started memberId=${memberDto.id}" }
        val cart = cartService.getCartForOrder(memberDto.id)
        val order = orderService.processOrderWithStockReduction(cart)
        val totalAmount = calculateFinalAmount(order, request.couponCode, memberDto.email)

        return processPaymentSession(memberDto.id, request, order, totalAmount)
    }

    private fun processPaymentSession(
        memberId: Long,
        request: CheckoutStartRequest,
        order: Order,
        totalAmount: BigDecimal,
    ): CheckoutStartResponse {
        return try {
            val paymentIntent = paymentService.createPaymentIntent(request, totalAmount)
            val payment = paymentService.createPayment(request, paymentIntent, order)
            log.info { "checkout.payment.created memberId=$memberId orderId=${order.id} paymentIntentId=${paymentIntent.id}" }
            CheckoutStartResponse(
                paymentIntentId = paymentIntent.id,
                orderId = order.id,
                amount = payment.amount,
                status = payment.status,
                clientSecret = paymentIntent.clientSecret,
                couponCode = request.couponCode,
            )
        } catch (exception: Exception) {
            val error = mapToPaymentError(exception)
            throw exception
        }
    }

    private fun calculateFinalAmount(
        order: Order,
        couponCode: String?,
        email: String,
    ): BigDecimal {
        var totalAmount = order.totalAmount

        couponCode?.let { couponCode ->
            try {
                val coupon = couponService.validate(couponCode, email)
                totalAmount = couponService.applyCoupon(coupon, order.totalAmount)
            } catch (exception: InvalidCouponException) {
                throw OrderNotProcessableException("can not apply coupon: ${exception.message}")
            }
        }
        return totalAmount
    }

    @Transactional(
        rollbackFor = [Exception::class],
        timeout = 30,
    )
    fun finalizeCheckout(
        member: MemberDto,
        request: FinalizePaymentRequest,
    ): PaymentResult {
        log.info { "checkout.finalize.started memberId=${member.id} orderId=${request.orderId} paymentIntentId=${request.paymentIntentId}" }
        val order = orderService.getValidatedPendingOrder(request.orderId, member.id)
        return try {
            validateOrderStatus(order)
            val paymentIntent = paymentService.confirmPaymentIntent(request.paymentIntentId)
            val status = updateOrderToPaid(order, paymentIntent)

            cartService.clearCart(member.id)
            val address = addressService.findAddressByMemberId(member.id)
            log.info { "checkout.finalize.success memberId=${member.id} orderId=${order.id} paymentStatus=$status" }
            PaymentResult.Success(orderId = order.id, paymentStatus = status, addressDto = address.toDto())
        } catch (exception: OrderAlreadyTerminatedException) {
            handleOrderAlreadyTerminated(order, exception)
        } catch (exception: Exception) {
            handleCheckoutFinalizeFailure(order, exception, member.email, request.couponKey)
        }
    }

    private fun handleOrderAlreadyTerminated(
        order: Order,
        exception: OrderAlreadyTerminatedException,
    ): PaymentResult.Failure {
        val paymentError =
            PaymentError(PaymentErrorCode.ORDER_ALREADY_TERMINATED)
                .copy(message = exception.message)
        return PaymentResult.Failure(order.id, paymentError)
    }

    private fun validateOrderStatus(order: Order) {
        if (order.status != OrderStatus.PENDING) {
            throw OrderAlreadyTerminatedException("order ${order.id} is already terminated.")
        }
    }

    private fun handleCheckoutFinalizeFailure(
        order: Order,
        exception: Throwable,
        memberEmail: String,
        couponCode: String?,
    ): PaymentResult.Failure {
        orderService.rollbackOptionsStock(order)
        couponService.rollbackCouponIfApplied(memberEmail, couponCode)
        val error = mapToPaymentError(exception)
        log.info { "checkout.rollback.completed orderId=${order.id} errorCode=${error.code}" }
        return PaymentResult.Failure(order.id, error)
    }

    private fun mapToPaymentError(exception: Throwable): PaymentError {
        return when (exception) {
            is PaymentException -> PaymentError(PaymentErrorCode.PAYMENT_FAILED)
            is StripeClientException -> PaymentError(PaymentErrorCode.STRIPE_CLIENT_ERROR)
            is StripeServerException -> PaymentError(PaymentErrorCode.STRIPE_SERVER_ERROR)
            is StripeProcessingException -> PaymentError(PaymentErrorCode.STRIPE_ERROR)
            else -> PaymentError(PaymentErrorCode.SYSTEM_ERROR)
        }.copy(message = exception.message)
    }

    private fun updateOrderToPaid(
        order: Order,
        paymentIntent: PaymentIntent,
    ): String {
        log.debug { "order.update.paid orderId=${order.id} paymentIntentId=${paymentIntent.id}" }
        when (paymentService.isPaymentSucceeded(paymentIntent)) {
            true -> {
                orderService.markOrderAsPaid(order)
                log.info { "order.marked.paid orderId=${order.id} status=${order.status}" }
                val payment = paymentService.markAsCompleted(paymentIntent.id)
                return payment.status.name
            }

            false -> throw PaymentStatusNotSuccessException("payment is not successful")
        }
    }
}
