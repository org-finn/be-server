package finn.orchestrator

import finn.auth.JwtValidator
import finn.response.auth.*
import finn.service.AuthService
import finn.service.JwtService
import finn.service.UserInfoService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class AuthOrchestrator(
    private val authService: AuthService,
    private val userInfoService: UserInfoService,
    private val jwtService: JwtService,
    private val jwtValidator: JwtValidator
) {

    fun getOAuthUserInfoForGoogle(authorizationCode: String): OAuthUserInfoResponse {
        val idTokenResponse = issueIdTokenForGoogle(authorizationCode)
        val payload = getPayloadFromGoogleIdToken(idTokenResponse.idToken)
        return OAuthUserInfoResponse("google", payload.sub, payload.email)
    }

    /**
     * 발급받은 ID Token을 기반으로 oAuthUser 접근
     * 최초 생성된 경우 회원가입 처리, oAuthUser & UserInfo 생성
     * 이미 생성된 경우 로그인 처리
     */
    @ExposedTransactional
    fun accessOAuthUser(
        provider: String,
        providerId: String,
        email: String
    ): UserInfoForTokenResponse {
        val userInfo = authService.checkExistByOAuthUser(providerId)

        if (userInfo == null) {
            val oAuthUserId = authService.createOAuthUser(provider, providerId, email)
            val createdUserInfo = userInfoService.createUserInfo(oAuthUserId)
            return UserInfoForTokenResponse(
                createdUserInfo.id,
                createdUserInfo.role.name,
                createdUserInfo.status.name
            )
        } else {
            return UserInfoForTokenResponse(userInfo.id, userInfo.role.name, userInfo.status.name)
        }
    }

    @ExposedTransactional
    fun issueToken(userId: UUID, role: String, status: String, deviceType: String): TokenResponse {
        val tokenResponse = jwtService.issue(userId, role, status, deviceType)
        return tokenResponse
    }

    @ExposedTransactional
    fun reIssueToken(
        refreshTokenString: String,
        deviceType: String
    ): TokenResponse {
        return jwtService.reIssue(refreshTokenString, deviceType)
    }

    @ExposedTransactional
    fun logout(
        accessToken: String,
        refreshTokenString: String
    ) {
        // 1. 액세스 토큰 무효화
        jwtService.addToBlacklist(accessToken)

        // 2. 리프레쉬 토큰 해제
        val refreshToken = jwtValidator.validateAndExtractRefreshToken(refreshTokenString)
        jwtService.releaseRefreshToken(refreshToken.deviceId)
    }

    /**
     * Google ID Token 발급 API로 요청
     */
    private fun issueIdTokenForGoogle(authorizationCode: String): GoogleIdTokenResponse {
        return authService.issueIdTokenForGoogle(authorizationCode)
    }

    /**
     * 발급받은 Google ID Token 디코딩 후 필요한 정보 획득
     */
    private fun getPayloadFromGoogleIdToken(idToken: String): GoogleIdTokenPayload {
        return authService.decodeIdToken(idToken)
    }

}