package techcourse.herobeans.dto

import techcourse.herobeans.entity.DiscountType
import java.math.BigDecimal
import java.time.LocalDateTime

class CouponResponse(
    val code: String,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val expiresAt: LocalDateTime?,
    val userMail: String?,
    val active: Boolean,
    val id: Long = 0,
)
