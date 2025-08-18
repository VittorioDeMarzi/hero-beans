package techcourse.herobeans.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import techcourse.herobeans.enums.OrderStatus
import techcourse.herobeans.enums.ShippingMethod
import techcourse.herobeans.mapper.CartItemMapper.toOrderItems
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener::class)
class Order private constructor(
    @Column(nullable = false)
    val memberId: Long,
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val orderItems: MutableList<OrderItem> = mutableListOf(),
    /**
     * Sum of all item (price * quantity), excluding shipping.
     * numeric(12,2): up to 10 digits before decimal and 2 after.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    var coffeeSubTotal: BigDecimal = BigDecimal.ZERO,
    /**
     * Monetary shipping fee for this order (computed from shippingMethod).
     */
    @Column(nullable = false, precision = 12, scale = 2)
    var shippingFee: BigDecimal = BigDecimal.ZERO,
    /**
     * Payment provider intent ID (e.g., Stripe PaymentIntent).
     * Null until created.
     */
    var paymentIntentId: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var shippingMethod: ShippingMethod = ShippingMethod.STANDARD,
    /**
     * Entity timestamps.
     * - createdAt: when the order was first persisted
     * - lastUpdatedAt: updated on every modification
     * - shippedAt: when the order was shipped (set by domain logic)
     * - deliveredAt: when the order was delivered (set by domain logic)
     */
    var shippedAt: LocalDateTime? = null,
    var deliveredAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    final lateinit var createdAt: LocalDateTime
        private set

    @LastModifiedDate
    @Column(nullable = false)
    final lateinit var lastUpdatedAt: LocalDateTime
        private set

    private fun addItemsFromCart(cart: Cart): Order {
        require(cart.items.isNotEmpty()) { "Items must not be empty" }

        this.orderItems.addAll(cart.items.toOrderItems(this))
        coffeeSubTotal = this.orderItems.sumOf { it.price }
        return this
    }

    fun markAs(status: OrderStatus) {
        this.status = status
    }

    companion object {
        fun fromCart(cart: Cart): Order {
            val order = Order(memberId = cart.member.id!!)
            return order.addItemsFromCart(cart)
        }
    }
}
