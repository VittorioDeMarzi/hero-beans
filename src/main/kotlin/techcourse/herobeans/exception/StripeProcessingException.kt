package techcourse.herobeans.exception

open class StripeProcessingException(message: String, override val cause: Throwable?) : RuntimeException(message, cause)

class StripeClientException(message: String, override val cause: Throwable?) :StripeProcessingException(message, cause)

class StripeServerException(message: String, override val cause: Throwable?) :StripeProcessingException(message, cause)
