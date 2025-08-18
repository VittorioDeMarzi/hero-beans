package techcourse.herobeans.dto

import techcourse.herobeans.entity.PaymentStatus
import java.math.BigDecimal

class StartCheckoutResponse(
    val paymentIntentId: String,
    val orderId: Long,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val clientSecret: String,
)
