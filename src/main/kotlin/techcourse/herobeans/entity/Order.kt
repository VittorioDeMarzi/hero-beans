package techcourse.herobeans.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import techcourse.herobeans.enums.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
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
     * Final total = coffeeSubTotal + shippingFee.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,
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
    var createdAt: LocalDateTime? = null,
    var lastUpdatedAt: LocalDateTime? = null,
    var shippedAt: LocalDateTime? = null,
    var deliveredAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    fun addItem(item: OrderItem) {
        item.order = this
        orderItems.add(item)
    }

    /**
     * Recalculate monetary totals:
     * - coffeeSubTotal = sum(price * quantity) for all items
     * - shippingFee = derived from shippingMethod and coffeeSubTotal
     * - totalAmount = coffeeSubTotal + shippingFee
     */
    fun recalculateTotals() {
        coffeeSubTotal = orderItems.sumOf { it.price * BigDecimal(it.quantity) }
        shippingFee = shippingMethod.feeForSubtotal(coffeeSubTotal)
        totalAmount = coffeeSubTotal + shippingFee
    }

    /**
     * Lifecycle hook called before the entity is first persisted.
     * Initializes timestamps and ensures totals are consistent.
     */
    @PrePersist
    fun onCreate() {
        val now = LocalDateTime.now()
        createdAt = now
        lastUpdatedAt = now
        recalculateTotals()
    }

    /**
     * Lifecycle hook called before each update.
     * Refreshes lastUpdatedAt and recalculates totals for consistency.
     */
    @PreUpdate
    fun onUpdate() {
        lastUpdatedAt = LocalDateTime.now()
        recalculateTotals()
    }
}
