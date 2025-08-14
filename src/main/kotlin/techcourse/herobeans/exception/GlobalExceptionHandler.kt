package techcourse.herobeans.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(
        value = [
            EmailAlreadyUsedException::class,
        ],
    )
    fun handleConflict(ex: RuntimeException) = buildErrorResponse(HttpStatus.CONFLICT, ex)

    fun buildErrorResponse(
        status: HttpStatus,
        ex: RuntimeException,
    ): ResponseEntity<ErrorMessageModel> {
        logger.error("Exception caught: ${ex.message}", ex)
        ex.cause?.let { cause ->
            logger.error("Caused by: ${cause.message}", cause)
        }
        val errorMessage = ErrorMessageModel(status.value(), ex.message)
        return ResponseEntity(errorMessage, status)
    }
}
