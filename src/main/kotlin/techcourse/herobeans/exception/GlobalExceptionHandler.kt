package techcourse.herobeans.exception

import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.slf4j.MDC
import org.springframework.core.convert.ConversionFailedException
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Instant

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler(private val env: Environment? = null) {
    private val isDev get() = env?.activeProfiles?.any { it.equals("dev", true) } == true

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<Map<String, String>> {
        val errors =
            ex.bindingResult.fieldErrors.associate {
                it.field to (it.defaultMessage ?: "Invalid value")
            }.also {
                log.warn { "Validation failed at ${request.requestURI}: $it" }
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
    fun handleConflict(
        ex: RuntimeException,
        request: HttpServletRequest,
    ) = buildErrorResponse(HttpStatus.CONFLICT, ex, request)

    @ExceptionHandler(
        value = [
            EmailOrPasswordIncorrectException::class,
            ForbiddenAccessException::class,
        ],
    )
    fun handleForbidden(
        ex: RuntimeException,
        request: HttpServletRequest,
    ) = buildErrorResponse(HttpStatus.FORBIDDEN, ex, request)

    @ExceptionHandler(UnauthorizedAccessException::class)
    fun handleUnauthorizedException(
        ex: UnauthorizedAccessException,
        request: HttpServletRequest,
    ) = buildErrorResponse(HttpStatus.UNAUTHORIZED, ex, request)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(
        ex: NotFoundException,
        request: HttpServletRequest,
    ) = buildErrorResponse(HttpStatus.NOT_FOUND, ex, request)

    @ExceptionHandler(
        value = [
            CartEmptyException::class,
            MaxAddressesExceededException::class,
            InsufficientStockException::class,
            IllegalArgumentException::class,
            ConversionFailedException::class,
            MethodArgumentTypeMismatchException::class,
            HttpMessageNotReadableException::class,
        ],
    )
    fun handleBadRequest(
        ex: RuntimeException,
        request: HttpServletRequest,
    ) = buildErrorResponse(HttpStatus.BAD_REQUEST, ex, request)

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorMessageModel> {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex)
    }
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest,
    ) = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request)

    private fun buildErrorResponse(
        status: HttpStatus,
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorMessageModel> {
        if (isDev) {
            log.error(ex) { "Exception at ${request.method} ${request.requestURI}: ${ex.message}" }
        } else {
            log.error { "Exception at ${request.method} ${request.requestURI}: ${ex.message}" }
        }

        val body = createErrorMessage(status, ex, request)
        return ResponseEntity.status(status).body(body)
    }

    private fun createErrorMessage(
        status: HttpStatus,
        ex: Exception,
        request: HttpServletRequest,
    ): ErrorMessageModel {
        val isInternal = status.is5xxServerError
        return ErrorMessageModel(
            timestamp = Instant.now().toString(),
            status = status.value(),
            error = status.reasonPhrase,
            message = if (isInternal) "Internal server error" else ex.message,
            path = request.requestURI,
            correlationId = MDC.get("correlationId"),
        )
    }
}
