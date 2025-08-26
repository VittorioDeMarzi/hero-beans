package techcourse.herobeans.exception

open class NotFoundException(message: String, override val cause: Throwable? = null) : RuntimeException(message, cause)

class OrderNotFoundException(message: String, override val cause: Throwable? = null) : NotFoundException(message, cause)

class PaymentNotFoundException(message: String, cause: Throwable? = null) : NotFoundException(message, cause)
