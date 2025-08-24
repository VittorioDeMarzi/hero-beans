package techcourse.herobeans.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime

// TODO: deactivate or delete on usage
@Entity
class Coupon(
    @Column(nullable = false)
    val code: String,
    @Enumerated(EnumType.ORDINAL)
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    @Column(nullable = true)
    val expiresAt: LocalDateTime? = null,
    @Column(nullable = true)
    val userMail: String? = null,
    var active: Boolean = true,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) {
    init {
        require(discountValue > BigDecimal.ZERO) { "discountValue must be positive" }
        if (discountType == DiscountType.PERCENTAGE) {
            require(discountValue > BigDecimal.ZERO && discountValue <= BigDecimal(100)) {
                "invalid discount percentage"
            }
        }
    }

    fun calculateDiscountedTotal(orderTotal: BigDecimal): BigDecimal {
        return when (this.discountType) {
            DiscountType.PERCENTAGE -> applyPercentageDiscount(orderTotal, this.discountValue)
            DiscountType.FIXED -> orderTotal - this.discountValue
        }.coerceAtLeast(0.0.toBigDecimal()).setScale(2, RoundingMode.HALF_UP)
    }

    companion object {
        fun createWelcomeCoupon(userMail: String) =
            Coupon(
                code = "WELCOME@BEAN",
                discountType = DiscountType.PERCENTAGE,
                discountValue = BigDecimal("10.00"),
                userMail = userMail,
                expiresAt = LocalDateTime.now() + Duration.ofDays(30),
                active = true,
            )
    }
}

// TODO: could be called in order process when someone buys above 5 KGs e.g. 15%
fun applyPercentageDiscount(
    orderTotal: BigDecimal,
    discountPercent: BigDecimal,
): BigDecimal {
    // E.g.: 10% -> 0.10
    val discountFraction = discountPercent.divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
    // Discount: total * (1 - discountFraction)
    val discountedTotal = orderTotal.multiply(BigDecimal.ONE.subtract(discountFraction))
    // Round to 2 decimal places for currency
    return discountedTotal.setScale(2, RoundingMode.HALF_UP)
}

enum class DiscountType {
    PERCENTAGE,
    FIXED,
}
