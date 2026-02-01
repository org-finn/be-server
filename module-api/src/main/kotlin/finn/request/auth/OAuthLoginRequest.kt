package finn.request.auth

data class OAuthLoginRequest(
    val authorizationCode: String,
    val deviceType: String
)