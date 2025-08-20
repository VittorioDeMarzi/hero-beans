package techcourse.herobeans.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.entity.Coupon
import techcourse.herobeans.entity.DiscountType
import techcourse.herobeans.exception.InvalidCouponException
import techcourse.herobeans.repository.CouponJpaRepository
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime

@Service
class CouponService(private val couponJpaRepository: CouponJpaRepository) {
    @Transactional(readOnly = true)
    fun validate(
        code: String,
        orderTotal: BigDecimal,
        userMail: String,
    ): Coupon {
        val coupon =
            couponJpaRepository.findByCodeAndActiveTrue(code)
                ?: throw InvalidCouponException("Invalid coupon code")

        coupon.userMail?.let {
            if (it != userMail) {
                throw InvalidCouponException("Invalid user email")
            }
        }

        coupon.expiresAt?.let {
            if (!it.isBefore(LocalDateTime.now())) {
                throw InvalidCouponException("Coupon has expired")
            }
        }

        return coupon
    }

    @Transactional
    fun apply(
        coupon: Coupon,
        orderTotal: BigDecimal,
    ): BigDecimal {
        val discountedTotal = coupon.calculateDiscountedTotal(orderTotal)
        coupon.active = false
        return discountedTotal
    }

    @Transactional
    fun createWelcomeCoupon(userMail: String): Coupon {
        val coupon =
            couponJpaRepository.save(
                Coupon(
                    code = "WELCOME@BEAN",
                    discountType = DiscountType.PERCENTAGE,
                    discountValue = BigDecimal("10.00"),
                    userMail = userMail,
                    expiresAt = LocalDateTime.now() + Duration.ofDays(30),
                    active = true,
                ),
            )
        return coupon
    }
}
