package finn.response.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true) // 필요한 필드만 매핑하고 나머진 무시
data class GoogleIdTokenPayload(
    @JsonProperty("sub") val sub: String,      // 구글의 유저 고유 ID (providerId)
    @JsonProperty("email") val email: String,  // 이메일
    @JsonProperty("name") val name: String?,   // 이름 (선택)
    @JsonProperty("picture") val picture: String? // 프로필 사진 (선택)
)