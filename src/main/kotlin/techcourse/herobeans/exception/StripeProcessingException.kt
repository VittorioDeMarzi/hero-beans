package techcourse.herobeans.exception

sealed class StripeProcessingException(message: String, override val cause: Throwable?) : RuntimeException(message, cause)

class StripeClientException(message: String, override val cause: Throwable?) : StripeProcessingException(message, cause)

class StripeServerException(message: String, override val cause: Throwable?) : StripeProcessingException(message, cause)

class PaymentIntentNotFoundException(message: String, override val cause: Throwable? = null) : StripeProcessingException(message, cause)

class IntentNotValidException(message: String, override val cause: Throwable? = null) : StripeProcessingException(message, cause)
