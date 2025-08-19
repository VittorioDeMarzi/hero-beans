package techcourse.herobeans.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import techcourse.herobeans.client.StripeClient
import techcourse.herobeans.dto.FinalizePaymentRequest
import techcourse.herobeans.dto.FinalizePaymentResponse
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.dto.StartCheckoutRequest
import techcourse.herobeans.dto.StartCheckoutResponse
import techcourse.herobeans.entity.Cart
import techcourse.herobeans.entity.Order
import techcourse.herobeans.entity.Payment
import techcourse.herobeans.enums.OrderStatus
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.repository.MemberJpaRepository
import techcourse.herobeans.repository.OptionJpaRepository
import techcourse.herobeans.repository.OrderJpaRepository
import java.math.BigDecimal

// TODO: overall, need to change all throw-exception or some logic to create exception
@Service
class CheckoutService(
    private val stripeClient: StripeClient,
    private val orderRepository: OrderJpaRepository,
    private val optionRepository: OptionJpaRepository,
    private val memberJpaRepository: MemberJpaRepository,
    private val orderService: OrderService,
) {
    @Transactional
    fun startOrder(
        memberDto: MemberDto,
        request: StartCheckoutRequest,
    ): StartCheckoutResponse {
        // TODO: wait Ann's PR
        //  val cart = cartService.getCartForOrder(member.id)
        val member = memberJpaRepository.findById(memberDto.id).orElseThrow { NotFoundException("Exception") }
        val cart = Cart(member)
        // TODO: delete line 38-39 if apply

        val order = orderService.processOrderWithStockReduction(cart)

        val paymentIntent = stripeClient.createPaymentIntent(request, order.totalAmount)

        val payment =
            Payment(
                amount = BigDecimal(paymentIntent.amount),
                paymentMethod = request.paymentMethod,
                paymentIntentId = paymentIntent.id,
                order = order,
            )
        // TODO: save payment

        return StartCheckoutResponse(
            paymentIntentId = paymentIntent.id,
            orderId = order.id,
            amount = payment.amount,
            status = payment.status,
            clientSecret = paymentIntent.clientSecret,
        )
    }

    fun finalizeOrder(request: FinalizePaymentRequest): FinalizePaymentResponse {
        val order =
            orderRepository.findById(request.orderId)
                .orElseThrow { NotFoundException("Order ${request.orderId} not found") }
        return try {
            val paymentIntent = stripeClient.confirmPaymentIntent(request.paymentIntentId)

            val status = succeededOrRollback(order, paymentIntent)
            orderRepository.save(order)
            return FinalizePaymentResponse(order.id, paymentStatus = status.name)
        } catch (e: Exception) {
            rollbackStockReduction(order)
            FinalizePaymentResponse(request.orderId, paymentStatus = "Payment failed") // TODO: payment status
        }
    }

    private fun succeededOrRollback(
        order: Order,
        paymentIntent: PaymentIntent,
    ): OrderStatus {
        return when (paymentIntent.status) {
            "succeeded" -> {
                order.status = OrderStatus.PAID
                OrderStatus.PAID
            }

            "requires_payment_method", "canceled" -> {
                order.status = OrderStatus.PAYMENT_FAILED
                OrderStatus.PAYMENT_FAILED
                rollbackStockReduction(order)
            }

            else -> {
                order.status
                rollbackStockReduction(order)
            }
            // TODO: make it clearer, separate private method
        }
    }

    private fun rollbackStockReduction(order: Order): OrderStatus {
        val optionIds = order.orderItems.map { it.optionId }
        val lockedOptions =
            optionRepository.findByIdsWithLock(optionIds)
                ?: throw IllegalArgumentException("Option IDs don't match between request and cart")

        order.orderItems.forEach { orderItem ->
            val option =
                lockedOptions.find { it.id == orderItem.optionId }
                    ?: throw NotFoundException("Option ${orderItem.optionId} not found in locked options")
            option.increaseQuantity(orderItem.quantity)
        }

        optionRepository.saveAll(lockedOptions)
        orderRepository.save(order)
        return order.status
    }

    private fun validatePaymentSuccess(
        paymentIntent: PaymentIntent,
        order: Order,
    ) {
        requireNotNull(order) { "Order cannot be null" }

        when {
            paymentIntent.status != "succeeded" ->
                throw IllegalStateException("Payment not successful: ${paymentIntent.status}")
        }
    }
}
