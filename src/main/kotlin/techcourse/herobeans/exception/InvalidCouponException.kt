package techcourse.herobeans.exception

// TODO: handle it in Order or GlobalExceptionHandler
class InvalidCouponException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
