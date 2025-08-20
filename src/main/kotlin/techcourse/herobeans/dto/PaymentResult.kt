package techcourse.herobeans.dto

sealed class PaymentResult(
    val response: FinalizePaymentResponse,
) {
    class Success(orderId: Long?, paymentStatus: String?) : PaymentResult(FinalizePaymentResponse(orderId, paymentStatus))

    class Failure(orderId: Long?, val error: PaymentError) : PaymentResult(FinalizePaymentResponse(orderId))
}

data class PaymentError(
    val code: PaymentErrorCode,
    private val message: String? = null,
    val cause: Throwable? = null,
) {
    fun message(msg: String?): PaymentError = this.copy(message = msg)
}

enum class PaymentErrorCode() {
    STRIPE_CLIENT_ERROR,
    STRIPE_SERVER_ERROR,
    STRIPE_ERROR,
    PAYMENT_FAILED,
    SYSTEM_ERROR,
    TIMEOUT,
}
