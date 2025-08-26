package techcourse.herobeans.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.entity.Coupon
import techcourse.herobeans.exception.InvalidCouponException
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.repository.CouponJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class CouponService(
    private val couponJpaRepository: CouponJpaRepository,
    private val memberJpaRepository: MemberJpaRepository,
) {
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
    fun applyCoupon(
        coupon: Coupon,
        orderTotal: BigDecimal,
    ): BigDecimal {
        val discountedTotal = coupon.calculateDiscountedTotal(orderTotal)
        coupon.active = false
        return discountedTotal
    }

    @Transactional
    fun createWelcomeCoupon(userMail: String): Coupon {
        return couponJpaRepository.save(Coupon.createWelcomeCoupon(userMail))
    }

    @Transactional
    fun getAllCouponsForUser(email: String): List<Coupon> {
        return couponJpaRepository.findAllByUserMail(email)
    }

    fun rollbackCouponIfApplied(
        memberEmail: String,
        couponCode: String?,
    ) {
        couponCode?.let { couponCode ->
            val coupon =
                couponJpaRepository.findByCodeAndUserMail(memberEmail, couponCode)
                    ?: throw NotFoundException("can not found coupon code $couponCode")
            coupon.active = true
            couponJpaRepository.save(coupon)
        }
    }
}
