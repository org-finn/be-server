package finn.config

import finn.auth.JwtValidator
import finn.auth.filter.JwtAuthenticationFilter
import finn.config.resolver.UserIdArgumentResolver
import finn.service.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Value("\${cors.allowed-origins}")
    private val allowedOrigins: String,
    private val jwtValidator: JwtValidator,
    private val jwtService: JwtService,
    private val userIdArgumentResolver: UserIdArgumentResolver
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**") // /api/ 경로 하위의 모든 엔드포인트에 적용
            .allowedOriginPatterns(*allowedOrigins.split(",").toTypedArray()) // 허용할 도메인
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
            .allowedHeaders("*") // 허용할 헤더
            .allowCredentials(true) // 쿠키 등 자격 증명 허용
            .maxAge(3600) // pre-flight 요청의 캐시 시간(초)
    }

    @Bean
    fun jwtFilter(): FilterRegistrationBean<JwtAuthenticationFilter> {
        val registrationBean = FilterRegistrationBean<JwtAuthenticationFilter>()

        // 필터 생성 및 의존성 주입
        registrationBean.filter = JwtAuthenticationFilter(
            jwtValidator,
            jwtService
        )

        // 필터 적용 순서 (낮을수록 먼저 실행)
        registrationBean.order = 1

        // 모든 URL에 필터 적용 (내부에서 화이트리스트로 필터링)
        registrationBean.addUrlPatterns("/api/*")

        return registrationBean
    }


    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(userIdArgumentResolver)
    }

}