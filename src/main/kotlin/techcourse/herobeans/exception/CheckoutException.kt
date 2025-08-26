package techcourse.herobeans.exception

sealed class CheckoutException(message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause)

class OrderAlreadyTerminatedException(message: String, override val cause: Throwable? = null) :
    CheckoutException(message, cause)
