package finn.request.auth

data class LogoutRequest(
    val deviceType: String,
    val refreshToken: String,
)
