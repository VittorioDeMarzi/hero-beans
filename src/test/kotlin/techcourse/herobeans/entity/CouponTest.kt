package techcourse.herobeans.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
class CouponTest {
    @Test
    fun `applyPercentageDiscount should return correct discounted value`() {
        val coupon =
            Coupon(
                code = "TEST",
                discountType = DiscountType.PERCENTAGE,
                discountValue = BigDecimal("10"),
            )

        val discounted = applyPercentageDiscount(100.toBigDecimal(), coupon.discountValue)
        assertEquals(90.toBigDecimal().setScale(2), discounted)
    }

    @Test
    fun `calculateDiscountedTotal for fixed amount should return correct discounted value`() {
        val coupon =
            Coupon(
                code = "TEST",
                discountType = DiscountType.FIXED,
                discountValue = BigDecimal("50"),
            )

        val discounted = coupon.calculateDiscountedTotal(200.toBigDecimal())
        assertEquals(150.toBigDecimal().setScale(2), discounted)
    }

    @Test
    fun `should throw exception if coupon does not to have percentage between 0 and 100`() {
        assertThrows<IllegalArgumentException> {
            Coupon(
                code = "WELCOME",
                discountType = DiscountType.PERCENTAGE,
                discountValue = BigDecimal("-10"),
                expiresAt = LocalDateTime.now().plusDays(1),
                userMail = "test@mail.com",
                active = true,
            )
        }
    }
}
