package techcourse.herobeans.service

import org.springframework.stereotype.Service
import techcourse.herobeans.entity.Cart
import techcourse.herobeans.entity.Order
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.enums.ShippingMethod
import techcourse.herobeans.exception.CartEmptyException
import techcourse.herobeans.exception.OrderDataInconsistencyException
import techcourse.herobeans.exception.OrderNotFoundException
import techcourse.herobeans.exception.UnauthorizedAccessException
import techcourse.herobeans.mapper.CartItemMapper.toOrderItems
import techcourse.herobeans.repository.OrderJpaRepository

@Service
class OrderService(
    private val orderRepository: OrderJpaRepository,
    private val optionService: PackageOptionService,
) {
    /**
     * Processes order with pessimistic lock to prevent stock concurrency issues.
     * The lock on options is held until the transaction completes.
     */
    fun processOrderWithStockReduction(cart: Cart): Order {
        val lockedOptions = getOptionsWithLock(cart)
        val shipping = ShippingMethod.getShippingPolicy(cart.totalAmount)

        val order =
            Order(
                cart.member.id,
                orderItems = cart.items.toOrderItems().toMutableList(),
                coffeeSubTotal = cart.totalAmount,
                shippingFee = shipping.shippingFee,
                shippingMethod = shipping,
            )
        val savedOrder = orderRepository.save(order)
        val updatedOptions = decreaseOptionsStock(savedOrder, lockedOptions)
        optionService.saveAll(updatedOptions)
        return savedOrder
    }

    private fun getOptionsWithLock(cart: Cart): List<PackageOption> {
        val ids =
            cart.items.map { it.option.id }
                .takeIf { it.isNotEmpty() }
                ?: throw CartEmptyException("Cart is empty")
        return optionService.findByIdsWithLock(ids)
    }

    private fun decreaseOptionsStock(
        order: Order,
        lockedOptions: List<PackageOption>,
    ): List<PackageOption> {
        val optionMap = lockedOptions.associateBy { it.id }

        return order.orderItems.map { orderItem ->
            val liveOption =
                optionMap[orderItem.optionId]
                    ?: throw OrderDataInconsistencyException(
                        "try to decease Option " +
                            "${orderItem.optionId}, but not found in locked options",
                    )

            liveOption.apply { decreaseQuantity(orderItem.quantity) }
        }
    }

    private fun increaseOptionsStock(
        order: Order,
        lockedOptions: List<PackageOption>,
    ): List<PackageOption> {
        val optionMap = lockedOptions.associateBy { it.id }

        return order.orderItems.map { orderItem ->
            val liveOption =
                optionMap[orderItem.optionId]
                    ?: throw OrderDataInconsistencyException(
                        "try to increase Option " +
                            "${orderItem.optionId}, but not found in locked options",
                    )

            liveOption.apply { increaseQuantity(orderItem.quantity) }
        }
    }

    fun rollbackOptionsStock(order: Order) {
        val optionIds = order.orderItems.map { it.optionId }
        val lockedOptions = optionService.findByIdsWithLock(optionIds)

        increaseOptionsStock(order, lockedOptions)

        optionService.saveAll(lockedOptions)
        order.markAsPaymentFailed()
        orderRepository.save(order)
    }

    fun getValidatedPendingOrder(
        orderId: Long,
        memberId: Long,
    ): Order {
        val order =
            orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow { OrderNotFoundException("Order $orderId not found") }
        validateOrder(order, memberId)
        return order
    }

    private fun validateOrder(
        order: Order,
        memberId: Long,
    ) {
        when {
            order.memberId != memberId ->
                throw UnauthorizedAccessException("Member does not have authorization for this order")
        }
    }

    fun markOrderAsPaid(order: Order) {
        order.markAsPaid()
        orderRepository.save(order)
    }
}
