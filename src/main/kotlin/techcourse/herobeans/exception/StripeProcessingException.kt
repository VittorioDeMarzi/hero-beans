package techcourse.herobeans.exception

class StripeProcessingException(message: String, override val cause: Throwable?) : RuntimeException(message, cause)
