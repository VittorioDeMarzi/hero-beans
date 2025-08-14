package techcourse.herobeans.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.dto.LoginRequest
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.dto.TokenResponse
import techcourse.herobeans.service.AuthenticationService

@RestController
@RequestMapping("/api/members")
class AuthenticationController(private val authenticationService: AuthenticationService) {
    /**
     * ex) request sample
     *
     POST /api/members/register HTTP/1.1
     Content-Type: application/json
     host: localhost:8080

     {
     "email": "admin@email.com",
     "password": "password"
     }
     */
    @PostMapping("/register")
    fun registerMember(
        @Valid @RequestBody request: RegistrationRequest,
    ): ResponseEntity<TokenResponse> {
        val token = authenticationService.registration(request)
        return ResponseEntity.ok(token)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
    ): ResponseEntity<TokenResponse> {
        val token = authenticationService.logIn(request)
        return ResponseEntity.ok(token)
    }
}
