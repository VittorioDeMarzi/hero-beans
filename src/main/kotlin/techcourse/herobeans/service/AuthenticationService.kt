package techcourse.herobeans.service

import mu.KotlinLogging
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

private val log = KotlinLogging.logger {}

@Transactional
@Service
class AuthenticationService(
    private val tokenService: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val memberJpaRepository: MemberJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val emailService: EmailService,
) {
    fun register(request: RegistrationRequest): TokenResponse {
        if (memberJpaRepository.existsByEmail(request.email)) {
            throw EmailAlreadyUsedException("Email already exists: ${request.email}")
        }
        val hashedPassword = passwordEncoder.encode(request.password)
        val saved =
            memberJpaRepository.save(Member(request.name, request.email, hashedPassword))
                .also { m -> log.info { "auth.registered memberId=${m.id}" } }
        val token =
            tokenService.createToken(saved.email)
                .also { log.info { "auth.token.issued memberId=${saved.id}" } }
        applicationEventPublisher.publishEvent(UserRegisteredEvent(request.email, request.name))
        return TokenResponse(token)
    }

    fun login(request: LoginRequest): TokenResponse {
        val member = memberJpaRepository.findByEmail(request.email) ?: throw UnauthorizedAccessException("Member is not registered yet")

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw EmailOrPasswordIncorrectException("Invalid password for email")
        }
        val token =
            tokenService.createToken(member.email)
                .also { log.info { "auth.login.succeeded memberId=${member.id}" } }
        return TokenResponse(token)
    }

    fun findMyInfo(id: Long): MemberDto {
        val member = memberJpaRepository.findById(id).orElseThrow { NotFoundException("Member with id $id not found") }
        return member.toDto()
    }
}
