package finn.request.auth

data class ReIssueRequest(
    val deviceType: String,
    val refreshToken: String? // app만 제출 필요
)
