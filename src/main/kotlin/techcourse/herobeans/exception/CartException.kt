package techcourse.herobeans.exception

sealed class CartException(message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause)

class CartEmptyException(message: String, override val cause: Throwable? = null) :
    CartException(message, cause)
