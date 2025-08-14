package techcourse.herobeans.controllers

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.dtos.RegistrationRequest
import techcourse.herobeans.dtos.TokenResponse
import techcourse.herobeans.services.AuthenticationService

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
}
