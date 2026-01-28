package finn.response.auth

data class OAuthUserInfoResponse(
    val provider: String,
    val providerId: String,
    val email: String
)
