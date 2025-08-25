package techcourse.herobeans.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
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
import techcourse.herobeans.exception.PaymentException
import techcourse.herobeans.exception.PaymentStatusNotSuccessException
import techcourse.herobeans.exception.StripeClientException
import techcourse.herobeans.exception.StripeProcessingException
import techcourse.herobeans.exception.StripeServerException

// TODO: overall, need to change all throw-exception or some logic to create exception
@Service
class CheckoutService(
    private val orderService: OrderService,
    private val paymentService: PaymentService,
    private val cartService: CartService,
) {
    // TODO: timeout setting
    //  @Transactional(rollbackFor = [Exception::class])
    @Transactional
    fun startCheckout(
        memberDto: MemberDto,
        request: CheckoutStartRequest,
    ): CheckoutStartResponse {
        val cart = cartService.getCartForOrder(memberDto.id)
        val order = orderService.processOrderWithStockReduction(cart)
        return try {
            val paymentIntent = paymentService.createPaymentIntent(request, order.totalAmount)
            val payment = paymentService.createPayment(request, paymentIntent, order)

            CheckoutStartResponse(
                paymentIntentId = paymentIntent.id,
                orderId = order.id,
                amount = payment.amount,
                status = payment.status,
                clientSecret = paymentIntent.clientSecret,
            )
        } catch (exception: Exception) {
            val error = mapToPaymentError(exception)
            throw exception
        }
    }

    @Transactional
    fun finalizeCheckout(
        member: MemberDto,
        request: FinalizePaymentRequest,
    ): PaymentResult {
        val order = orderService.getValidatedPendingOrder(request.orderId, member.id)
        return try {
            val paymentIntent = paymentService.confirmPaymentIntent(request.paymentIntentId)
            val status = updateOrderToPaid(order, paymentIntent)
            cartService.clearCart(member.id)
            PaymentResult.Success(orderId = order.id, paymentStatus = status.name)
        } catch (exception: Exception) {
            handleCheckoutFinalizeFailure(order, exception)
        }
    }

    private fun handleCheckoutFinalizeFailure(
        order: Order,
        exception: Throwable,
    ): PaymentResult.Failure {
        orderService.rollbackOptionsStock(order)
        val error = mapToPaymentError(exception)

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
    ): OrderStatus {
        when (paymentService.isPaymentSucceeded(paymentIntent)) {
            true -> {
                orderService.markOrderAsPaid(order)
                return order.status
            }

            false -> throw PaymentStatusNotSuccessException("payment is not successful")
        }
    }
}
