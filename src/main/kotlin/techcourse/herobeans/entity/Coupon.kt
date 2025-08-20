package techcourse.herobeans.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

/**
 * TODO:
 * - userMail for welcome coupons
 * - welcome coupons have the same code, but are tied to a user
 * - deactivate or delete on usage
 * - event handler for registration
 */
@Entity
class Coupon(
    @Column(unique = true, nullable = false)
    val code: String,
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
    // TODO: could be called in order when someone buys above 5 KGs e.g. 15%
    fun applyPercentageDiscount(
        orderTotal: BigDecimal,
        discountPercent: BigDecimal,
    ): BigDecimal {
        // e.g.: 10% -> 0.10
        val discountFraction = discountPercent.divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
        // Discount: total * (1 - discountFraction)
        val discountedTotal = orderTotal.multiply(BigDecimal.ONE.subtract(discountFraction))
        // Round to 2 decimal places for currency
        return discountedTotal.setScale(2, RoundingMode.HALF_UP)
    }

    fun calculateDiscountedTotal(orderTotal: BigDecimal) =
        when (this.discountType) {
            DiscountType.PERCENTAGE -> orderTotal * applyPercentageDiscount(orderTotal, this.discountValue)
            DiscountType.FIXED -> orderTotal - this.discountValue
        }.coerceAtLeast(0.0.toBigDecimal())
}

enum class DiscountType {
    PERCENTAGE,
    FIXED,
}

// object CouponGenerator {
//    private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
//    private val random = SecureRandom()
//
//    fun generateCode(length: Int = 8): String =
//        (1..length)
//            .map { CHARACTERS[random.nextInt(CHARACTERS.length)] }
//            .joinToString("")
//
//    fun generatePrefixedCoupon(prefix: String = "WELCOME", length: Int = 4): String {
//        val randomPostfix = this.generateCode(length)
//        return "$prefix-$randomPostfix"
//    }
// }
