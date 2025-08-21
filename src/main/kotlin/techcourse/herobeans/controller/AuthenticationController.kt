package techcourse.herobeans.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.annotation.LoginMember
import techcourse.herobeans.dto.LoginRequest
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.dto.TokenResponse
import techcourse.herobeans.service.AuthenticationService

private val log = KotlinLogging.logger {}

@Tag(name = "Auth", description = "Member registration, login, and profile")
@RestController
@RequestMapping("/api/members")
class AuthenticationController(private val authenticationService: AuthenticationService) {
    @Operation(summary = "Register a new member")
    @PostMapping("/register")
    fun registerMember(
        @Valid @RequestBody request: RegistrationRequest,
    ): ResponseEntity<TokenResponse> {
        log.info { "api.auth.register requested" }
        val token = authenticationService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(token)
    }

    @Operation(summary = "Login and get JWT")
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
    ): ResponseEntity<TokenResponse> {
        log.info { "api.auth.login requested" }
        val token = authenticationService.login(request)
        return ResponseEntity.ok(token)
    }

    @Operation(summary = "Get my profile")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    fun findMyInfo(
        @LoginMember member: MemberDto,
    ): ResponseEntity<MemberDto> {
        log.info { "api.auth.me requested memberId=${member.id}" }
        val info = authenticationService.findMyInfo(member.id)
        return ResponseEntity.ok(info)
    }
}
