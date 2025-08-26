package techcourse.herobeans.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.entity.Coupon
import techcourse.herobeans.exception.InvalidCouponException
import techcourse.herobeans.repository.CouponJpaRepository
import java.math.BigDecimal
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

@Service
class CouponService(private val couponJpaRepository: CouponJpaRepository) {
    @Transactional(readOnly = true)
    fun validate(
        code: String,
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
            if (it.isBefore(LocalDateTime.now())) {
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
        return couponJpaRepository.save(Coupon.createWelcomeCoupon(userMail)).also {
            log.info { "WelcomeCoupon ${it.id} created - user: $userMail}" }
        }
    }

    @Transactional
    fun getAllCouponsForUser(email: String): List<Coupon> {
        return couponJpaRepository.findAllByUserMail(email)
    }
}
