package techcourse.herobeans.dto

sealed class PaymentResult() {
    data class Success(val orderId: Long, val paymentStatus: String, val addressDto: AddressDto) : PaymentResult()

    data class Failure(val orderId: Long?, val error: PaymentError) : PaymentResult()
}

data class PaymentError(
    val codeDetail: PaymentErrorCode,
    val code: String? = null,
    val message: String? = null,
    val cause: Throwable? = null,
) {
    fun message(msg: String?): PaymentError = this.copy(message = msg)
}

enum class PaymentErrorCode() {
    STRIPE_CLIENT_ERROR,
    STRIPE_SERVER_ERROR,
    STRIPE_ERROR,
    STRIPE_MESSAGE_ERROR,
    PAYMENT_FAILED,
    SYSTEM_ERROR,
    TIMEOUT,
}
