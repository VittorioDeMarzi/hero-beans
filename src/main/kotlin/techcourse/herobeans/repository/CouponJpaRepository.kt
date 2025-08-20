package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import techcourse.herobeans.entity.Coupon

interface CouponJpaRepository : JpaRepository<Coupon, Long> {
    fun findByCodeAndActiveTrue(code: String): Coupon?

    fun findByUserMail(userMail: String): List<Coupon>
}
