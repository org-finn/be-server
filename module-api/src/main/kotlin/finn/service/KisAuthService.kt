package finn.service

import com.fasterxml.jackson.databind.JsonNode
import finn.config.kis.KisProperties
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

/**
 * webSocket 접속용 approval_key를 발급
 */
@Service
class KisAuthService(
    private val kisProperties: KisProperties,
    builder: RestClient.Builder
) {
    private val restClient = builder.baseUrl(kisProperties.restBaseUrl).build()

    // 웹소켓 접속용 Approval Key 발급
    fun getWebsocketApprovalKey(): String {
        val requestBody = mapOf(
            "grant_type" to "client_credentials",
            "appkey" to kisProperties.appKey,
            "secretkey" to kisProperties.secretKey
        )

        val response = restClient.post()
            .uri("/oauth2/Approval")
            .body(requestBody)
            .retrieve()
            .body<JsonNode>()

        // API 응답에서 approval_key 추출
        return response?.get("approval_key")?.asText()
            ?: throw RuntimeException("Failed to get approval key from KIS")
    }
}