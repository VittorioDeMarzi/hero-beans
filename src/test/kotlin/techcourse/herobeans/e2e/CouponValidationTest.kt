package techcourse.herobeans.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.entity.Coupon
import techcourse.herobeans.entity.DiscountType
import techcourse.herobeans.exception.InvalidCouponException
import techcourse.herobeans.repository.CouponJpaRepository
import techcourse.herobeans.service.CouponService
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CouponValidationTest() {
    @Autowired
    private lateinit var couponJpaRepository: CouponJpaRepository

    @Autowired
    private lateinit var couponService: CouponService

    @Test
    fun `valid coupon is returned`() {
        val coupon =
            couponJpaRepository.save(
                Coupon(
                    code = "SUMMER",
                    discountType = DiscountType.FIXED,
                    discountValue = BigDecimal("10"),
                    userMail = "user@test.com",
                    expiresAt = LocalDateTime.now().plusDays(1),
                ),
            )

        val result = couponService.validate("SUMMER", "user@test.com")

        assertEquals(coupon.id, result.id)
    }

    @Test
    fun `invalid code throws exception`() {
        assertThrows<InvalidCouponException> {
            couponService.validate("DOES_NOT_EXIST", "user@test.com")
        }
    }

    @Test
    fun `wrong user throws exception`() {
        couponJpaRepository.save(
            Coupon(
                code = "PRIVATE",
                discountType = DiscountType.FIXED,
                discountValue = BigDecimal("5"),
                userMail = "owner@test.com",
                expiresAt = LocalDateTime.now().plusDays(1),
            ),
        )

        val ex =
            assertThrows<InvalidCouponException> {
                couponService.validate("PRIVATE", "someone@test.com")
            }
        assertEquals("Invalid user email", ex.message)
    }

    @Test
    fun `expired coupon throws exception`() {
        couponJpaRepository.save(
            Coupon(
                code = "OLD",
                discountType = DiscountType.PERCENTAGE,
                discountValue = BigDecimal("20"),
                expiresAt = LocalDateTime.now().minusDays(1),
            ),
        )

        val ex =
            assertThrows<InvalidCouponException> {
                couponService.validate("OLD", "user@test.com")
            }
        assertEquals("Coupon has expired", ex.message)
    }

    @Test
    fun `active coupon should be valid for any user`() {
        val coupon =
            couponJpaRepository.save(
                Coupon(
                    code = "FOR_EVERYONE",
                    discountType = DiscountType.FIXED,
                    discountValue = BigDecimal("15"),
                    expiresAt = null,
                    userMail = null,
                ),
            )

        val result = couponService.validate("FOR_EVERYONE", "random@test.com")

        assertEquals(coupon.id, result.id)
    }
}
