package techcourse.herobeans.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
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
                "/api/member/**",
                "/api/members/me",
                "/api/admin/**",
                "/api/payments/**",
                "/api/address/**",
                "/api/me/coupons/**",
                "/api/checkout/**",
            )
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver?>) {
        resolvers.add(loginMemberArgumentResolver)
        resolvers.add(adminOnlyResolver)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://herobeans.com")
            .allowedMethods("GET", "POST", "HEAD", "OPTIONS", "DELETE", "PUT", "PATCH")
            .allowedHeaders("*")
            .exposedHeaders("Location")
            .maxAge(1800)
    }
}
