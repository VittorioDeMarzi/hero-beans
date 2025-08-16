package techcourse.herobeans.controller

import ecommerce.annotation.LoginMember
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.dto.LoginRequest
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.dto.TokenResponse
import techcourse.herobeans.service.AuthenticationService

@RestController
@RequestMapping("/api/members")
class AuthenticationController(private val authenticationService: AuthenticationService) {
    @PostMapping("/register")
    fun registerMember(
        @Valid @RequestBody request: RegistrationRequest,
    ): ResponseEntity<TokenResponse> {
        val token = authenticationService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(token)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
    ): ResponseEntity<TokenResponse> {
        val token = authenticationService.login(request)
        return ResponseEntity.ok(token)
    }

    @GetMapping("/me")
    fun login(
        @LoginMember member: MemberDto,
    ): ResponseEntity<MemberDto> {
        val info = authenticationService.findMyInfo(member.id ?: 0L)
        return ResponseEntity.ok(info)
    }
}
