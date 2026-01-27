package finn.response.auth

import java.util.*

data class ClientTokenResponse(
    val accessToken: String,
    val refreshToken: String?, // app은 바디로 반환
    val deviceId: UUID
)
