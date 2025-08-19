package techcourse.herobeans.service

import org.springframework.stereotype.Service
import techcourse.herobeans.entity.Cart
import techcourse.herobeans.entity.Order
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.enums.ShippingMethod
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.mapper.CartItemMapper.toOrderItems
import techcourse.herobeans.repository.OptionJpaRepository
import techcourse.herobeans.repository.OrderJpaRepository

@Service
class OrderService(
    private val orderRepository: OrderJpaRepository,
    private val optionRepository: OptionJpaRepository,
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
        optionRepository.saveAll(updatedOptions)
        return savedOrder
    }

    private fun getOptionsWithLock(cart: Cart): List<PackageOption> {
        val ids =
            cart.items.map { it.option.id }
                .takeIf { it.isNotEmpty() }
                ?: throw NotFoundException("Cart has no valid options")
        return optionRepository.findByIdsWithLock(ids)
            ?: throw NotFoundException("Option IDs don't match between request and cart")
    }

    private fun decreaseOptionsStock(
        order: Order,
        lockedOptions: List<PackageOption>,
    ): List<PackageOption> {
        val optionMap = lockedOptions.associateBy { it.id }

        return order.orderItems.map { orderItem ->
            val liveOption =
                optionMap[orderItem.optionId]
                    ?: throw NotFoundException("Option ${orderItem.optionId} not found in locked options")

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
                    ?: throw NotFoundException("Option ${orderItem.optionId} not found in locked options")

            liveOption.apply { increaseQuantity(orderItem.quantity) }
        }
    }

    fun rollbackOptionsStock(order: Order) {
        val optionIds = order.orderItems.map { it.optionId }
        val lockedOptions = findByIdsWithLock(optionIds)

        increaseOptionsStock(order, lockedOptions)

        optionRepository.saveAll(lockedOptions)
        orderRepository.save(order)
    }

    fun findOrderByIdWithItems(orderId: Long): Order {
        return orderRepository.findByIdWithOrderItems(orderId)
            .orElseThrow { NotFoundException("Order $orderId not found") }
    }

    fun findByIdsWithLock(ids: List<Long>): List<PackageOption> {
        return optionRepository.findByIdsWithLock(ids)
            ?: throw NotFoundException("Option IDs don't match between requested locked options")
    }

    fun markOrderAsPaid(order: Order) {
        order.markAsPaid()
        orderRepository.save(order)
    }
}
