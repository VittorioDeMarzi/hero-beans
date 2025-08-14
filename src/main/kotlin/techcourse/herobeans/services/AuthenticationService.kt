package techcourse.herobeans.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.configurations.JwtTokenProvider
import techcourse.herobeans.configurations.PasswordEncoder
import techcourse.herobeans.dtos.RegistrationRequest
import techcourse.herobeans.dtos.TokenResponse
import techcourse.herobeans.exceptions.MemberEmailAlreadyExistsException
import techcourse.herobeans.model.Member
import techcourse.herobeans.repositories.MemberJpaRepository

@Service
class AuthenticationService(
    private val tokenService: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val memberJpaRepository: MemberJpaRepository,
) {
    @Transactional
    fun registration(request: RegistrationRequest): TokenResponse {
        if (memberJpaRepository.existsByEmail(request.email)) {
            throw MemberEmailAlreadyExistsException("Email already exists: ${request.email}")
        }
        val hashedPassword = passwordEncoder.encode(request.password)
        memberJpaRepository.save(Member(request.name, request.email, hashedPassword))
        val token = tokenService.createToken(request.email)
        return TokenResponse(token)
    }
}
