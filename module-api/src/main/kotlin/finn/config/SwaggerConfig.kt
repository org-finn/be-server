package finn.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        // 1. Security Scheme 정의 (JWT 설정)
        val jwtSchemeName = "Bearer Authentication"

        val securityScheme = SecurityScheme()
            .name(jwtSchemeName)
            .type(SecurityScheme.Type.HTTP) // HTTP 방식
            .scheme("bearer")               // Bearer 토큰
            .bearerFormat("JWT")            // 토큰 형식

        // 2. Security Requirement 정의 (모든 API에 적용할 설정)
        val securityRequirement = SecurityRequirement().addList(jwtSchemeName)

        // 3. OpenAPI 객체 빌드
        return OpenAPI()
            .components(Components().addSecuritySchemes(jwtSchemeName, securityScheme))
            .addSecurityItem(securityRequirement) // 전역으로 보안 설정 적용
            .info(apiInfo())
    }

    private fun apiInfo() = Info()
        .title("Finn API Documentation")
        .description("Finn 서비스 API 명세서입니다.")
        .version("v1.0.0")
}