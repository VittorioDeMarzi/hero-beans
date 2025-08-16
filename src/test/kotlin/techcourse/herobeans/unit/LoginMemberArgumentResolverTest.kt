package techcourse.herobeans.unit

import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import techcourse.herobeans.configuration.LoginMemberArgumentResolver
import techcourse.herobeans.entity.Member
import techcourse.herobeans.enums.MemberRole
import techcourse.herobeans.exception.UnauthorizedAccessException
import techcourse.herobeans.repository.MemberJpaRepository

class LoginMemberArgumentResolverTest {
    private val memberRepository: MemberJpaRepository = Mockito.mock(MemberJpaRepository::class.java)
    private val resolver = LoginMemberArgumentResolver(memberRepository)

    private val mavContainer: ModelAndViewContainer? = null
    private val binderFactory: WebDataBinderFactory? = null
    private val methodParameter: MethodParameter = Mockito.mock(MethodParameter::class.java)

    @Test
    fun `should return member dto when successful`() {
        val email = "member@example.com"
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.getAttribute("email")).thenReturn(email)

        val webRequest = Mockito.mock(NativeWebRequest::class.java)
        Mockito.`when`(webRequest.getNativeRequest(HttpServletRequest::class.java)).thenReturn(request)

        val admin = Member(email = email, role = MemberRole.USER, name = "Ordinary User", password = "password")
        Mockito.`when`(memberRepository.findByEmail(email)).thenReturn(admin)

        val dto = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory)

        Assertions.assertThat(email).isEqualTo(dto.email)
        Assertions.assertThat(MemberRole.USER).isEqualTo(dto.role)
    }

    // Todo: maybe change to IllegalStateException
    @Test
    fun `should throw UnauthorizedAccessException when no HttpServletRequest`() {
        val webRequest = Mockito.mock(NativeWebRequest::class.java)
        Mockito.`when`(webRequest.getNativeRequest(HttpServletRequest::class.java)).thenReturn(null)

        assertThrows<UnauthorizedAccessException> {
            resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory)
        }
    }

    // Todo: maybe change to IllegalStateException
    @Test
    fun `should throw UnauthorizedAccessException when no email attribute`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.getAttribute("email")).thenReturn(null)

        val webRequest = Mockito.mock(NativeWebRequest::class.java)
        Mockito.`when`(webRequest.getNativeRequest(HttpServletRequest::class.java)).thenReturn(request)

        assertThrows<UnauthorizedAccessException> {
            resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory)
        }
    }

    @Test
    fun `should throw UnauthorizedAccessException when member not found`() {
        val email = "missing@example.com"
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.getAttribute("email")).thenReturn(email)

        val webRequest = Mockito.mock(NativeWebRequest::class.java)
        Mockito.`when`(webRequest.getNativeRequest(HttpServletRequest::class.java)).thenReturn(request)
        Mockito.`when`(memberRepository.findByEmail(email)).thenReturn(null)

        assertThrows<UnauthorizedAccessException> {
            resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory)
        }
    }
}
