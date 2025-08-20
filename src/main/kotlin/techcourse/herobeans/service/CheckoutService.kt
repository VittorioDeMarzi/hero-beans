package techcourse.herobeans.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import techcourse.herobeans.dto.FinalizePaymentRequest
import techcourse.herobeans.dto.FinalizePaymentResponse
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.dto.StartCheckoutRequest
import techcourse.herobeans.dto.StartCheckoutResponse
import techcourse.herobeans.entity.Order
import techcourse.herobeans.enums.OrderStatus
import techcourse.herobeans.exception.PaymentProcessingException
import techcourse.herobeans.repository.MemberJpaRepository

// TODO: overall, need to change all throw-exception or some logic to create exception
@Service
class CheckoutService(
    private val memberJpaRepository: MemberJpaRepository,
    private val orderService: OrderService,
    private val paymentService: PaymentService,
    private val cartService: CartService,
) {
    // TODO: timeout setting
    //  @Transactional(rollbackFor = [Exception::class])
    @Transactional
    fun startCheckout(
        memberDto: MemberDto,
        request: StartCheckoutRequest,
    ): StartCheckoutResponse {
        val cart = cartService.getCartForOrder(memberDto.id)
        val order = orderService.processOrderWithStockReduction(cart)
        val paymentIntent = paymentService.createPaymentIntent(request, order.totalAmount)
        val payment = paymentService.createPayment(request, paymentIntent, order)

        return StartCheckoutResponse(
            paymentIntentId = paymentIntent.id,
            orderId = order.id,
            amount = payment.amount,
            status = payment.status,
            clientSecret = paymentIntent.clientSecret,
        )
    }

    @Transactional
    fun finalizeCheckout(
        member: MemberDto,
        request: FinalizePaymentRequest,
    ): FinalizePaymentResponse {
        val order = orderService.findOrderByIdWithItems(request.orderId)
        if (order.memberId != member.id) throw PaymentProcessingException("order found, but") // TODO: better message
        return try {
            val paymentIntent = paymentService.confirmPaymentIntent(request.paymentIntentId)
            val status = succeededOrRollback(order, paymentIntent)
            return FinalizePaymentResponse(order.id, paymentStatus = status.name)
        } catch (e: Exception) {
            // TODO: handle this exception
            orderService.rollbackOptionsStock(order)
            FinalizePaymentResponse(
                request.orderId,
                paymentStatus = "Payment failed",
            ) // TODO: need to change payment status
        } // TODO: PaymentProcessingException?
    }

    private fun succeededOrRollback(
        order: Order,
        paymentIntent: PaymentIntent,
    ): OrderStatus {
        return when (paymentService.isPaymentSucceeded(paymentIntent)) {
            true -> handleSucceededPayment(order)
            false -> handleNotSucceededPayment(paymentIntent.status, order)
        }
    }

    private fun handleSucceededPayment(order: Order): OrderStatus {
        orderService.markOrderAsPaid(order)
        return order.status
    }

    private fun handleNotSucceededPayment(
        paymentIntentPayment: String,
        order: Order,
    ): OrderStatus {
        when (paymentIntentPayment) {
            // TODO: I need to check spelling of "canceled" in stripe
            "canceled" -> order.markAsCancelled()
            else -> order.markAsPaymentFailed()
        }
        orderService.rollbackOptionsStock(order)
        return order.status
    }
}
