package finn.response.auth

data class ClientLoginResponse(
    val accessToken: String,
    val refreshToken: String?, // app은 바디로 반환
    val isNewUser: Boolean
)
