package finn.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**") // /api/ 경로 하위의 모든 엔드포인트에 적용
            .allowedOrigins("https://articker.kr") // 허용할 프론트엔드 도메인
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
            .allowedHeaders("*") // 허용할 헤더
            .allowCredentials(true) // 쿠키 등 자격 증명 허용
            .maxAge(3600) // pre-flight 요청의 캐시 시간(초)
    }
}