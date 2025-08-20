package techcourse.herobeans.configuration

import ecommerce.annotation.AdminOnly
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import techcourse.herobeans.enums.MemberRole
import techcourse.herobeans.exception.ForbiddenAccessException
import techcourse.herobeans.exception.UnauthorizedAccessException
import techcourse.herobeans.mapper.MemberMapper.toDto
import techcourse.herobeans.repository.MemberJpaRepository

private val log = KotlinLogging.logger {}

@Component
class AdminOnlyResolver(
    private val memberRepository: MemberJpaRepository,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AdminOnly::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw UnauthorizedAccessException("No HttpServletRequest")
        val email =
            request.getAttribute("email") as? String
                ?: throw UnauthorizedAccessException("No authenticated member found 1")
        val member =
            memberRepository.findByEmail(email) ?: throw UnauthorizedAccessException("No authenticated member found 2")
        if (member.role != MemberRole.ADMIN) throw ForbiddenAccessException("Forbidden access. Admin only")
        log.info { "admin.guard.passed memberId=${member.id} path=${request.requestURI}" }
        return member.toDto()
    }
}
