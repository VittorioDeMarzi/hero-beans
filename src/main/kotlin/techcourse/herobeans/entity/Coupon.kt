package techcourse.herobeans.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Entity
data class Coupon(
    @Column(unique = true, nullable = false)
    val code: String,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val minOrderValue: BigDecimal? = null,
    val maxUse: Int? = null,
    var usageCount: Int = 0,
    val expiresAt: LocalDateTime? = null,
    val active: Boolean = true,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) {
    fun applyPercentageDiscount(
        orderTotal: BigDecimal,
        discountPercent: BigDecimal,
    ): BigDecimal {
        // From percent to fraction: 10% -> 0.10
        val discountFraction = discountPercent.divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
        // Discount: total * (1 - discountFraction)
        val discountedTotal = orderTotal.multiply(BigDecimal.ONE.subtract(discountFraction))
        // Round to 2 decimal places for currency
        return discountedTotal.setScale(2, RoundingMode.HALF_UP)
    }

    fun calculateDiscountTotal(orderTotal: BigDecimal) =
        when (this.discountType) {
            DiscountType.PERCENTAGE -> orderTotal * applyPercentageDiscount(orderTotal, this.discountValue)
            DiscountType.FIXED -> orderTotal - this.discountValue
        }.coerceAtLeast(0.0.toBigDecimal())
}

enum class DiscountType {
    PERCENTAGE,
    FIXED,
}
