package techcourse.herobeans.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.entity.Coupon
import techcourse.herobeans.exception.InvalidCouponException
import techcourse.herobeans.repository.CouponJpaRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class CouponService(private val couponJpaRepository: CouponJpaRepository) {
    @Transactional(readOnly = true)
    fun validate(
        code: String,
        orderTotal: BigDecimal,
    ): Coupon {
        val coupon =
            couponJpaRepository.findByCodeAndActiveTrue(code)
                ?: throw InvalidCouponException("Invalid coupon code")

        if (coupon.expiresAt != null && coupon.expiresAt!!.isBefore(LocalDateTime.now())) {
            throw InvalidCouponException("Coupon has expired")
        }

        if (coupon.minOrderValue != null && orderTotal < coupon.minOrderValue) {
            throw InvalidCouponException("Order total does not meet minimum value for this coupon")
        }

        if (coupon.maxUse != null && coupon.usageCount >= coupon.maxUse!!) {
            throw InvalidCouponException("Coupon usage limit reached")
        }

        return coupon
    }

    @Transactional
    fun apply(
        coupon: Coupon,
        orderTotal: BigDecimal,
    ): BigDecimal {
        val discountedTotal = coupon.calculateDiscountTotal(orderTotal)
        coupon.usageCount += 1
        couponJpaRepository.save(coupon)

        return discountedTotal
    }
}
