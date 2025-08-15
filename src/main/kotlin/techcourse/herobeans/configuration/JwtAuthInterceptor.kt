package techcourse.herobeans.configuration

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import techcourse.herobeans.exception.UnauthorizedAccessException
import kotlin.text.substringAfter

@Component
class JwtAuthInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val accessToken =
            request.getHeader("Authorization")?.substringAfter("Bearer ")
                ?: throw UnauthorizedAccessException("Invalid Token")
        if (!jwtTokenProvider.validateToken(accessToken)) throw UnauthorizedAccessException("Invalid Token")

        val email = jwtTokenProvider.getEmailFromToken(accessToken) ?: throw UnauthorizedAccessException("Invalid Token")
        request.setAttribute("email", email)
        return true
    }
}
