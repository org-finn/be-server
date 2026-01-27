package finn.controller

import finn.apiSpec.AuthApiSpec
import finn.orchestrator.AuthOrchestrator
import finn.request.auth.OAuthLoginRequest
import finn.response.SuccessResponse
import finn.response.auth.ClientTokenResponse
import finn.service.TokenCookieService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authOrchestrator: AuthOrchestrator,
    private val tokenCookieService: TokenCookieService
) : AuthApiSpec {

    override fun loginForGoogleOAuth(
        oAuthLoginRequest: OAuthLoginRequest
    ): ResponseEntity<SuccessResponse<ClientTokenResponse>> {
        // 1. 인가코드로 구글 oAuth2에 ID 토큰 발급 후 oauth 정보 추출
        val oAuthUserInfo =
            authOrchestrator.getOAuthUserInfoForGoogle(oAuthLoginRequest.authorizationCode)
        // 2. 정보 기반으로 oauth_user 접근, 최초 회원일 경우 oauth_user 생성
        // 2-1. 최초 회원일 경우 user_info 생성 후 회원가입 처리
        val userInfo = authOrchestrator.accessOAuthUser(
            oAuthUserInfo.provider,
            oAuthUserInfo.providerId,
            oAuthUserInfo.email
        )
        // 3. 액세스/리프레쉬 토큰 생성하여 리턴
        val response = authOrchestrator.issueToken(
            userInfo.userId,
            userInfo.role,
            userInfo.status,
            oAuthLoginRequest.deviceType
        )
        // 4. web일 경우 쿠키에 토큰 set, 기존 필드 null로 설정
        if (tokenCookieService.checkDeviceType(oAuthLoginRequest.deviceType)) {
            val cookie = tokenCookieService.setRefreshTokenInCookie(response.refreshToken)
            val responseForWeb = ClientTokenResponse(response.accessToken, null, response.deviceId)
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(SuccessResponse("200 Ok", "구글 로그인 수행 후 액세스/리프레쉬 토큰 발급 성공", responseForWeb))
        }
        val responseForApp =
            ClientTokenResponse(response.accessToken, response.refreshToken, response.deviceId)
        return ResponseEntity.ok()
            .body(SuccessResponse("200 Ok", "구글 로그인 수행 후 액세스/리프레쉬 토큰 발급 성공", responseForApp))
    }
}