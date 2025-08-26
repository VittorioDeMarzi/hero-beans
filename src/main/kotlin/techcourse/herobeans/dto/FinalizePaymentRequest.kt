package techcourse.herobeans.dto

class FinalizePaymentRequest(
    val addressId: Long,
    val paymentIntentId: String,
    val orderId: Long,
    val couponKey: String? = null,
)
