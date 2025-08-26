package techcourse.herobeans.dto

class CheckoutStartRequest(
    val addressId: Long,
    val paymentMethod: String,
    val couponCode: String? = null,
)
