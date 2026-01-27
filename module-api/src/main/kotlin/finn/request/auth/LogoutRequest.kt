package finn.request.auth

import java.util.*

data class LogoutRequest(
    val deviceType: String,
    val deviceId: UUID
)
