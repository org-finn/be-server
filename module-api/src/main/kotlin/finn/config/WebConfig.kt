package finn.config

import com.fasterxml.jackson.databind.ObjectMapper
import finn.auth.JwtValidator
import finn.auth.filter.JwtAuthenticationFilter
import finn.config.resolver.UserIdArgumentResolver
import finn.config.security.ExceptionHandlerFilter
import finn.service.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Value("\${cors.allowed-origins}")
    private val allowedOrigins: String,
    private val jwtValidator: JwtValidator,
    private val jwtService: JwtService,
    private val userIdArgumentResolver: UserIdArgumentResolver,
    private val objectMapper: ObjectMapper
) : WebMvcConfigurer {

    @Bean
    fun corsFilter(): FilterRegistrationBean<CorsFilter> {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()

        config.allowCredentials = true
        allowedOrigins.split(",").forEach { config.addAllowedOriginPattern(it) }
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        config.maxAge = 3600L

        source.registerCorsConfiguration("/api/**", config)

        val registrationBean = FilterRegistrationBean(CorsFilter(source))
        registrationBean.order = 0 // 최상위 우선순위
        return registrationBean
    }

    @Bean
    fun exceptionHandlerFilter(): FilterRegistrationBean<ExceptionHandlerFilter> {
        val registrationBean = FilterRegistrationBean<ExceptionHandlerFilter>()
        registrationBean.filter = ExceptionHandlerFilter(objectMapper)
        registrationBean.order = 1
        registrationBean.addUrlPatterns("/*")
        return registrationBean
    }

    @Bean
    fun jwtAuthenticationFilter(): FilterRegistrationBean<JwtAuthenticationFilter> {
        val registrationBean = FilterRegistrationBean<JwtAuthenticationFilter>()
        // 필터 생성 및 의존성 주입
        registrationBean.filter = JwtAuthenticationFilter(
            jwtValidator,
            jwtService
        )

        // 필터 적용 순서 (낮을수록 먼저 실행)
        registrationBean.order = 2
        // 모든 URL에 필터 적용 (내부에서 화이트리스트로 필터링)
        registrationBean.addUrlPatterns("/api/*")
        return registrationBean
    }


    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(userIdArgumentResolver)
    }

}