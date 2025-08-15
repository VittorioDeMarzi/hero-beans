package techcourse.herobeans.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfigurer(
    private val jwtAuthInterceptor: JwtAuthInterceptor,
    private val loginMemberArgumentResolver: LoginMemberArgumentResolver,
    private val adminOnlyResolver: AdminOnlyResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(jwtAuthInterceptor)
            .addPathPatterns(
                "/api/user/wishes/**",
                "/api/admin/**",
                "api/admin/stats/**",
                "/api/payments/**",
            )
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver?>) {
        resolvers.add(loginMemberArgumentResolver)
        resolvers.add(adminOnlyResolver)
    }
}
