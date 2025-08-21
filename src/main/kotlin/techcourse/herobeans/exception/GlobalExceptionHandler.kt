package techcourse.herobeans.exception

import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionFailedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors =
            ex.bindingResult.fieldErrors.associate {
                it.field to (it.defaultMessage ?: "Invalid value")
            }
        return ResponseEntity.badRequest().body(errors)
    }

    @ExceptionHandler(
        value = [
            EmailAlreadyUsedException::class,
            CoffeeNameAlreadyExistsException::class,
            OrderDataInconsistencyException::class,
        ],
    )
    fun handleConflict(ex: RuntimeException) = buildErrorResponse(HttpStatus.CONFLICT, ex)

    @ExceptionHandler(
        value = [
            EmailOrPasswordIncorrectException::class,
            ForbiddenAccessException::class,
        ],
    )
    fun handleForbidden(ex: RuntimeException) = buildErrorResponse(HttpStatus.FORBIDDEN, ex)

    @ExceptionHandler(UnauthorizedAccessException::class)
    fun handleUnauthorizedException(ex: UnauthorizedAccessException) = buildErrorResponse(HttpStatus.UNAUTHORIZED, ex)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException) = buildErrorResponse(HttpStatus.NOT_FOUND, ex)

    @ExceptionHandler(
        value = [
            CartEmptyException::class,
            MaxAddressesExceededException::class,
            InsufficientStockException::class,
            IllegalArgumentException::class,
            ConversionFailedException::class,
            MethodArgumentTypeMismatchException::class,
        ],
    )
    fun handleBadRequest(ex: RuntimeException) = buildErrorResponse(HttpStatus.BAD_REQUEST, ex)

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorMessageModel> {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex)
    }

    fun buildErrorResponse(
        status: HttpStatus,
        ex: Exception,
    ): ResponseEntity<ErrorMessageModel> {
        logger.error("Exception caught: ${ex.message}", ex)
        ex.cause?.let { cause ->
            logger.error("Caused by: ${cause.message}", cause)
        }
        val errorMessage = ErrorMessageModel(status.value(), ex.message)
        return ResponseEntity(errorMessage, status)
    }
}
