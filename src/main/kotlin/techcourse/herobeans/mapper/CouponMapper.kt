package techcourse.herobeans.mapper

import techcourse.herobeans.dto.CouponResponse
import techcourse.herobeans.entity.Coupon

object CouponMapper {
    fun Coupon.toResponse(): CouponResponse {
        return CouponResponse(
            code = this.code,
            discountType = this.discountType,
            discountValue = this.discountValue,
            expiresAt = this.expiresAt,
            userMail = this.userMail,
            active = this.active,
            id = this.id,
        )
    }
}
