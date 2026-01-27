package finn.request.auth

import java.util.*

data class ReIssueRequest(
    val deviceType: String,
    val deviceId: UUID,
    val refreshToken: String? // app만 제출 필요
)
