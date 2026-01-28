package finn.response.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleIdTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("expires_in") val expiresIn: Int,
    @JsonProperty("scope") val scope: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("id_token") val idToken: String
)
