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
        val updatedOptions = decreaseStock(savedOrder, lockedOptions)
        optionRepository.saveAll(updatedOptions)
        return savedOrder
    }

    private fun getOptionsWithLock(cart: Cart): List<PackageOption> {
        val ids =
            cart.items.map { it.option.id }
                .takeIf { it.isNotEmpty() }
                ?: throw NotFoundException("Cart has no valid options")
        return optionRepository.findByIdsWithLock(ids)
            ?: throw IllegalArgumentException("Option IDs don't match between request and cart")
    }

    private fun decreaseStock(
        order: Order,
        lockedOptions: List<PackageOption>,
    ): List<PackageOption> {
        val optionMap = lockedOptions.associateBy { it.id }

        return order.orderItems.map { orderItem ->
            val liveOption =
                optionMap[orderItem.optionId]
                    ?: throw IllegalStateException("Option ${orderItem.optionId} not found in locked options")

            liveOption.apply { decreaseQuantity(orderItem.quantity) }
        }
    }
}
