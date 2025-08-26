package techcourse.herobeans.service

import mu.KotlinLogging
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

private val log = KotlinLogging.logger {}

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
        log.info { "order.process.started cartId=${cart.id} memberId=${cart.member.id}" }
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
        log.info { "order.process.success orderId=${savedOrder.id} memberId=${cart.member.id} shippingMethod=${shipping.name}" }
        return savedOrder
    }

    private fun getOptionsWithLock(cart: Cart): List<PackageOption> {
        log.info { "order.options.lock.started cartId=${cart.id}" }
        val ids =
            cart.items.map { it.option.id }
                .takeIf { it.isNotEmpty() }
                ?: throw CartEmptyException("Cart is empty")
        val lockedOptions = optionService.findByIdsWithLock(ids)
        log.info { "order.options.lock.success cartId=${cart.id} optionIds=$ids" }
        return lockedOptions
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
        log.info { "order.stock.decrease.started orderId=${order.id}" }
        val optionMap = lockedOptions.associateBy { it.id }

        val updatedOptions =
            order.orderItems.map { orderItem ->
                val liveOption =
                    optionMap[orderItem.optionId]
                        ?: throw OrderDataInconsistencyException(
                            "try to increase Option ${orderItem.optionId}, but not found in locked options",
                        )
                liveOption.apply { increaseQuantity(orderItem.quantity) }
            }
        log.info { "order.stock.increase.success orderId=${order.id} optionIds=${optionMap.keys}" }
        return updatedOptions
    }

    fun rollbackOptionsStock(order: Order) {
        log.info { "order.rollback.started orderId=${order.id}" }
        val optionIds = order.orderItems.map { it.optionId }
        val lockedOptions = optionService.findByIdsWithLock(optionIds)

        increaseOptionsStock(order, lockedOptions)

        optionService.saveAll(lockedOptions)
        order.markAsPaymentFailed()
        orderRepository.save(order)
        log.info { "order.rollback.success orderId=${order.id} optionIds=$optionIds" }
    }

    fun getValidatedPendingOrder(
        orderId: Long,
        memberId: Long,
    ): Order {
        log.info { "order.validate.started orderId=$orderId memberId=$memberId" }
        val order =
            orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow { OrderNotFoundException("Order $orderId not found") }
        validateOrder(order, memberId)
        log.info { "order.validate.success orderId=$orderId memberId=$memberId" }
        return order
    }

    private fun validateOrder(
        order: Order,
        memberId: Long,
    ) {
        log.info { "order.authorization.check.started orderId=${order.id} memberId=$memberId" }
        when {
            order.memberId != memberId ->
                throw UnauthorizedAccessException("Member does not have authorization for this order")
        }
        log.info { "order.authorization.check.success orderId=${order.id} memberId=$memberId" }
    }

    fun markOrderAsPaid(order: Order) {
        log.info { "order.mark.paid.started orderId=${order.id}" }
        order.markAsPaid()
        orderRepository.save(order)
        log.info { "order.mark.paid.success orderId=${order.id}" }
    }
}
