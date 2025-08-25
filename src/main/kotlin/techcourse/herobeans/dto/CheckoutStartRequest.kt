package techcourse.herobeans.dto

class CheckoutStartRequest(
    val addressDto: AddressDto,
    val paymentMethod: String,
    val couponCode: String? = null,
)
