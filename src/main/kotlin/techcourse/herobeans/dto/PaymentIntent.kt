package techcourse.herobeans.dto

class PaymentIntent(
    val id: String,
    val amount: Int,
    val status: String,
    val memberEmail: String,
    val clientSecret: String,
    val currency: String,
)
