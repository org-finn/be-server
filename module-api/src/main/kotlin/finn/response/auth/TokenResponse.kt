package finn.response.auth

import java.util.*

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val deviceId: UUID
)
