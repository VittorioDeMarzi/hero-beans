package techcourse.herobeans.sliceTest

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.exception.EmailAlreadyUsedException
import techcourse.herobeans.exception.ForbiddenAccessException
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.exception.UnauthorizedAccessException

@Validated
@RestController
@RequestMapping("/dummy")
@ActiveProfiles("test")
class FakeExceptionController {
    @GetMapping("/not-found/{id}")
    fun notFound(
        @PathVariable id: Long,
    ): String {
        throw NotFoundException("Entity not found: $id")
    }

    @GetMapping("/bad-request-type")
    fun badRequestType(
        @RequestParam @Min(1) count: Int,
    ): String = "ok"

    @PostMapping("/bad-json", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun badJson(
        @RequestBody body: Map<String, Any>,
    ): String = "ok"

    @GetMapping("/forbidden")
    fun forbidden(): String {
        throw ForbiddenAccessException("No access")
    }

    @GetMapping("/unauthorized")
    fun unauthorized(): String {
        throw UnauthorizedAccessException("Need auth")
    }

    @GetMapping("/conflict")
    fun conflict(): String {
        throw EmailAlreadyUsedException("Email is already used")
    }

    @GetMapping("/generic")
    fun generic(): String {
        error("Boom")
    }

    data class CreateDto(
        @field:Min(1) val amount: Int,
    )

    @PostMapping("/validation", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun validation(
        @Valid @RequestBody dto: CreateDto,
    ): String = "ok"
}
