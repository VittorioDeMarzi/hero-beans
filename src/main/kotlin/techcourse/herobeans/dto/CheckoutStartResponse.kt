package techcourse.herobeans.dto

import techcourse.herobeans.enums.PaymentStatus
import java.math.BigDecimal

class CheckoutStartResponse(
    val paymentIntentId: String,
    val orderId: Long,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val clientSecret: String?,
)
