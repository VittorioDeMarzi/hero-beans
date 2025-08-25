package techcourse.herobeans.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

private const val CORRELATION_HEADER = "X-Correlation-Id"
private val log = KotlinLogging.logger {}

@Component
class CorrelationIdFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val cid = request.getHeader(CORRELATION_HEADER)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        MDC.put("correlationId", cid)
        response.setHeader(CORRELATION_HEADER, cid)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
