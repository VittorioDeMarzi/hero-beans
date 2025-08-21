package techcourse.herobeans.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.configuration.JwtTokenProvider
import techcourse.herobeans.configuration.PasswordEncoder
import techcourse.herobeans.dto.LoginRequest
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.dto.TokenResponse
import techcourse.herobeans.entity.Member
import techcourse.herobeans.event.UserRegisteredEvent
import techcourse.herobeans.exception.EmailAlreadyUsedException
import techcourse.herobeans.exception.EmailOrPasswordIncorrectException
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.exception.UnauthorizedAccessException
import techcourse.herobeans.mapper.MemberMapper.toDto
import techcourse.herobeans.repository.MemberJpaRepository

@Transactional
@Service
class AuthenticationService(
    private val tokenService: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val memberJpaRepository: MemberJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun register(request: RegistrationRequest): TokenResponse {
        if (memberJpaRepository.existsByEmail(request.email)) {
            throw EmailAlreadyUsedException("Email already exists: ${request.email}")
        }
        val hashedPassword = passwordEncoder.encode(request.password)
        memberJpaRepository.save(Member(request.name, request.email, hashedPassword))
        val token = tokenService.createToken(request.email)
        // publish event for WelcomeCouponListener
        applicationEventPublisher.publishEvent(UserRegisteredEvent(request.email))
        return TokenResponse(token)
    }

    fun login(request: LoginRequest): TokenResponse {
        val member = memberJpaRepository.findByEmail(request.email) ?: throw UnauthorizedAccessException("Member is not registered yet")

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw EmailOrPasswordIncorrectException("Invalid password for email")
        }
        val token = tokenService.createToken(request.email)
        return TokenResponse(token)
    }

    fun findMyInfo(id: Long): MemberDto {
        val member = memberJpaRepository.findById(id).orElseThrow { NotFoundException("Member with id $id not found") }
        return member.toDto()
    }
}
