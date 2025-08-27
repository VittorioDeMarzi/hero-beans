package techcourse.herobeans.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.entity.Coupon
import techcourse.herobeans.exception.InvalidCouponException
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.repository.CouponJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository
import java.math.BigDecimal
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

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
        log.info { "coupon.validate.started code=$code userMail=$userMail" }
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
        log.info { "coupon.validate.success code=$code userMail=$userMail" }
        return coupon
    }

    @Transactional
    fun applyCoupon(
        coupon: Coupon,
        orderTotal: BigDecimal,
    ): BigDecimal {
        log.info { "coupon.apply.started couponId=${coupon.id} orderTotal=$orderTotal" }
        val discountedTotal = coupon.calculateDiscountedTotal(orderTotal)
        coupon.active = false
        log.info { "coupon.apply.success couponId=${coupon.id} discountedTotal=$discountedTotal" }
        return discountedTotal
    }

    @Transactional
    fun createWelcomeCoupon(userMail: String): Coupon {
        log.info { "coupon.create.welcome.started userMail=$userMail" }
        val coupon = couponJpaRepository.save(Coupon.createWelcomeCoupon(userMail))
        log.info { "coupon.create.welcome.success userMail=$userMail couponId=${coupon.id}" }
        return coupon
    }

    @Transactional
    fun getAllCouponsForUser(email: String): List<Coupon> {
        log.info { "coupon.list.started userMail=$email" }
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
