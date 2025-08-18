package techcourse.herobeans.service

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
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.entity.Payment
import techcourse.herobeans.enums.OrderStatus
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.repository.OptionJpaRepository
import techcourse.herobeans.repository.OrderJpaRepository
import java.math.BigDecimal

@Service
class CheckoutService(
    private val stripeClient: StripeClient,
    private val orderRepository: OrderJpaRepository,
    private val optionRepository: OptionJpaRepository,
) {
    @Transactional
    fun startOrder(
        member: MemberDto,
        request: StartCheckoutRequest,
    ): StartCheckoutResponse {
        // TODO: wait Ann's PR
        //  val cart = cartService.getCartForOrder(member.id)
        val cart = Cart()
        val order = processOrderWithStockReduction(cart)

        val paymentIntent = stripeClient.createPaymentIntent(request)

        val payment =
            Payment(
                amount = BigDecimal(paymentIntent.amount),
                paymentMethod = request.paymentMethod,
                paymentIntentId = paymentIntent.id,
            )
        return StartCheckoutResponse(
            paymentIntentId = paymentIntent.id,
            orderId = order.id,
            amount = payment.amount,
            status = payment.status,
            clientSecret = paymentIntent.clientSecret,
        )
    }

    /**
     * Processes order with pessimistic lock to prevent stock concurrency issues.
     * The lock on options is held until the transaction completes.
     */
    private fun processOrderWithStockReduction(cart: Cart): Order {
        val optionIds = extractValidOptionIds(cart)
        val lockedOptions = getOptionsWithLock(optionIds)

        val order = Order.fromCart(cart)
        val savedOrder = orderRepository.save(order)

        val updatedOptions = deductStock(savedOrder, lockedOptions)
        optionRepository.saveAll(updatedOptions)

        return savedOrder
    }

    private fun extractValidOptionIds(cart: Cart): List<Long> {
        return cart.items
            .map { it.option.id }
            .takeIf { it.isNotEmpty() }
            ?: throw NotFoundException("Cart has no valid options")
    }

    private fun getOptionsWithLock(ids: List<Long>): List<PackageOption> {
        return optionRepository.findByIdsWithLock(ids)
            ?: throw IllegalArgumentException("Option IDs don't match between request and cart")
    }

    private fun deductStock(
        order: Order,
        liveOptions: List<PackageOption>,
    ): List<PackageOption> {
        val optionMap = liveOptions.associateBy { it.id }

        return order.orderItems.map { orderItem ->
            val liveOption =
                optionMap[orderItem.optionId]
                    ?: throw IllegalStateException("Option ${orderItem.optionId} not found in locked options")

            liveOption.apply { decreaseQuantity(orderItem.quantity) }
        }
    }

    fun finalizeOrder(
        orderId: Long,
        request: FinalizePaymentRequest,
    ): FinalizePaymentResponse {
        // TODO: implement this method
        return FinalizePaymentResponse()
    }
}
