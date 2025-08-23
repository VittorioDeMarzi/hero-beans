package techcourse.herobeans.exception

sealed class OrderException(message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause)

class OrderDataInconsistencyException(message: String, override val cause: Throwable? = null) :
    OrderException(message, cause) // TODO: 422 Unprocessable Entity

class OrderNotProcessableException(message: String, override val cause: Throwable? = null) :
    OrderException(message, cause)
