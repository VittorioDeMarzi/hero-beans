package techcourse.herobeans.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import techcourse.herobeans.dto.FinalizePaymentRequest
import techcourse.herobeans.dto.FinalizePaymentResponse
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.dto.StartCheckoutRequest
import techcourse.herobeans.dto.StartCheckoutResponse
import techcourse.herobeans.entity.Cart
import techcourse.herobeans.entity.Order
import techcourse.herobeans.enums.OrderStatus
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.repository.MemberJpaRepository

// TODO: overall, need to change all throw-exception or some logic to create exception
@Service
class CheckoutService(
    private val memberJpaRepository: MemberJpaRepository,
    private val orderService: OrderService,
    private val paymentService: PaymentService,
) {
    // TODO: timeout setting
    //  @Transactional(rollbackFor = [Exception::class])
    @Transactional
    fun startOrder(
        memberDto: MemberDto,
        request: StartCheckoutRequest,
    ): StartCheckoutResponse {
        // TODO: wait Ann's PR
        //  val cart = cartService.getCartForOrder(member.id)
        val member =
            memberJpaRepository.findById(memberDto.id)
                .orElseThrow { NotFoundException("Exception") }
        val cart = Cart(member)
        // TODO: delete line 38-39 if apply

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
    fun finalizeOrder(request: FinalizePaymentRequest): FinalizePaymentResponse {
        val order = orderService.findOrderByIdWithItems(request.orderId)
        return try {
            val paymentIntent = paymentService.confirmPaymentIntent(request)
            val status = succeededOrRollback(order, paymentIntent)
            return FinalizePaymentResponse(order.id, paymentStatus = status.name)
        } catch (e: Exception) {
            // TODO: Should I throw something?
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
            "canceled" -> order.markAsCancelled()
            else -> order.markAsPaymentFailed()
        }
        orderService.rollbackOptionsStock(order)
        return order.status
    }
}
