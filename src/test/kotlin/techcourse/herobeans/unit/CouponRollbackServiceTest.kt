package techcourse.herobeans.unit

import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import techcourse.herobeans.entity.Coupon
import techcourse.herobeans.entity.DiscountType
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.repository.CouponJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository
import techcourse.herobeans.service.CouponService
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class CouponRollbackServiceTest {
    @MockK
    private lateinit var memberJpaRepository: MemberJpaRepository

    @MockK
    private lateinit var couponJpaRepository: CouponJpaRepository

    @InjectMockKs
    private lateinit var couponService: CouponService

    private val testMemberEmail = "test@example.com"
    private val testCouponCode = "DISCOUNT10"

    @Test
    fun `should do nothing when coupon code is null`() {
        couponService.rollbackCouponIfApplied(testMemberEmail, null)

        verify { memberJpaRepository wasNot Called }
        verify { couponJpaRepository wasNot Called }
    }

    @Test
    fun `should throw NotFoundException when coupon does not exist`() {
        every { couponJpaRepository.findByCodeAndUserMail(testMemberEmail, testCouponCode) } returns null

        val exception =
            assertThrows<NotFoundException> {
                couponService.rollbackCouponIfApplied(testMemberEmail, testCouponCode)
            }

        assertThat(exception.message).contains(testCouponCode)
        verify { memberJpaRepository wasNot Called }
    }

    @Test
    fun `should rollback coupon to active true when coupon exists`() {
        val coupon =
            Coupon(
                code = testCouponCode,
                userMail = testMemberEmail,
                discountValue = BigDecimal(10),
                discountType = DiscountType.PERCENTAGE,
                active = false,
            )
        val savedCouponSlot = slot<Coupon>()

        every { couponJpaRepository.findByCodeAndUserMail(testMemberEmail, testCouponCode) } returns coupon
        every { couponJpaRepository.save(capture(savedCouponSlot)) } returns coupon

        couponService.rollbackCouponIfApplied(testMemberEmail, testCouponCode)

        verify(exactly = 1) { couponJpaRepository.findByCodeAndUserMail(testMemberEmail, testCouponCode) }
        verify(exactly = 1) { couponJpaRepository.save(any()) }
        verify { memberJpaRepository wasNot Called }

        assertThat(savedCouponSlot.captured.active).isTrue()
    }

    @Test
    fun `should save coupon even when already active`() {
        val coupon =
            Coupon(
                code = testCouponCode,
                userMail = testMemberEmail,
                discountValue = BigDecimal(10),
                discountType = DiscountType.PERCENTAGE,
                active = true,
            )
        val savedCouponSlot = slot<Coupon>()

        every { couponJpaRepository.findByCodeAndUserMail(testMemberEmail, testCouponCode) } returns coupon
        every { couponJpaRepository.save(capture(savedCouponSlot)) } returns coupon

        couponService.rollbackCouponIfApplied(testMemberEmail, testCouponCode)

        assertThat(savedCouponSlot.captured.active).isTrue()
        verify(exactly = 1) { couponJpaRepository.save(any()) }
        verify { memberJpaRepository wasNot Called }
    }
}
