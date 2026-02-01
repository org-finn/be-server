package finn.response.auth

data class ReIssueResponse(
    val accessToken: String,
    val refreshToken: String?, // app은 바디로 반환
)