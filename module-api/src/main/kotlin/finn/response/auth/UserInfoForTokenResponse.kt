package finn.response.auth

import java.util.*

data class UserInfoForTokenResponse(
    val userId: UUID,
    val role: String,
    val status: String
)
