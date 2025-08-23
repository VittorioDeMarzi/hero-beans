package techcourse.herobeans.dto

class FinalizePaymentRequest(
    val paymentIntentId: String,
    val orderId: Long,
    val couponKey: String?,
    // TODO: implement class
)
