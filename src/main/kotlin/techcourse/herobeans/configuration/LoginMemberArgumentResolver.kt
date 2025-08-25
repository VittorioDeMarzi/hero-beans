package techcourse.herobeans.configuration

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import techcourse.herobeans.annotation.LoginMember
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.exception.UnauthorizedAccessException
import techcourse.herobeans.mapper.MemberMapper.toDto
import techcourse.herobeans.repository.MemberJpaRepository

@Component
class LoginMemberArgumentResolver(
    private val memberRepository: MemberJpaRepository,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(LoginMember::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): MemberDto {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw UnauthorizedAccessException("No HttpServletRequest")
        val email = request.getAttribute("email") as? String ?: throw UnauthorizedAccessException("No authenticated member found")
        val member = memberRepository.findByEmail(email) ?: throw UnauthorizedAccessException("No authenticated member found")
        return member.toDto()
    }
}
