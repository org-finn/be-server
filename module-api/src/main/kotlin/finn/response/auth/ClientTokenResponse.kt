package finn.response.auth

data class ClientTokenResponse(
    val accessToken: String,
    val refreshToken: String?, // app은 바디로 반환
)
