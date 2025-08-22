package techcourse.herobeans.exception

sealed class PaymentException(message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause)

class PaymentProcessingException(message: String, override val cause: Throwable? = null) :
    PaymentException(message, cause)

class PaymentSystemException(message: String, override val cause: Throwable? = null) :
    PaymentException(message, cause)

class PaymentStatusNotSuccessException(message: String, override val cause: Throwable? = null) :
    PaymentException(message, cause)
