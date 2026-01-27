package finn.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import finn.config.GoogleOAuthConfig
import finn.entity.UserInfo
import finn.exception.OAuthException
import finn.repository.OAuthUserRepository
import finn.response.auth.GoogleIdTokenPayload
import finn.response.auth.GoogleIdTokenResponse
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class AuthService(
    private val googleOAuthConfig: GoogleOAuthConfig,
    private val objectMapper: ObjectMapper,
    restClientBuilder: RestClient.Builder,
    private val oAuthUserRepository: OAuthUserRepository,
) {
    private val restClient = restClientBuilder.build()

    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun issueIdTokenForGoogle(authorizationCode: String): GoogleIdTokenResponse {
        // 1. 구글에 전송할 요청 바디 구성
        val requestBody = mapOf(
            "client_id" to googleOAuthConfig.clientId,
            "client_secret" to googleOAuthConfig.clientSecret,
            "code" to authorizationCode,
            "grant_type" to "authorization_code",
            "redirect_uri" to googleOAuthConfig.redirectUri
        )

        // 2. POST 요청 전송
        return restClient.post()
            .uri(googleOAuthConfig.tokenUri)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(GoogleIdTokenResponse::class.java)
            ?: throw OAuthException("Google ID Token 발급 실패: 응답이 비어있습니다.")

    }

    fun decodeIdToken(idToken: String): GoogleIdTokenPayload {
        try {
            // 1. JWT는 "."으로 구분됨 (Header.Payload.Signature)
            val parts = idToken.split(".")
            if (parts.size < 2) {
                throw RuntimeException("잘못된 형식의 id_token입니다.")
            }

            // 2. 두 번째 부분(Payload)을 Base64 URL 디코딩
            val payloadBytes = Base64.getUrlDecoder().decode(parts[1])
            val payloadString = String(payloadBytes, Charsets.UTF_8)

            // 3. JSON 문자열 -> DTO 변환
            return objectMapper.readValue<GoogleIdTokenPayload>(payloadString)

        } catch (e: Exception) {
            log.error { "id_token 디코딩 중 오류가 발생했습니다. " + e.message }
            throw OAuthException("소셜 로그인 과정 중 오류가 발생했습니다.")
        }
    }

    fun checkExistByOAuthUser(providerId: String): UserInfo? {
        return oAuthUserRepository.findByProviderId(providerId)
    }

    fun createOAuthUser(provider: String, providerId: String, email: String): UUID {
        return oAuthUserRepository.save(provider, providerId, email)
    }

}