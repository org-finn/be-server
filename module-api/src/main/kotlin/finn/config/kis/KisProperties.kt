package finn.config.kis

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kis")
data class KisProperties(
    val appKey: String,
    val secretKey: String,
    val restBaseUrl: String,
    val wsBaseUrl: String
)
