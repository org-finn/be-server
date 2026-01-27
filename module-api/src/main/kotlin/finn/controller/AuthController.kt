package finn.controller

import finn.apiSpec.AuthApiSpec
import finn.exception.auth.InvalidTokenException
import finn.orchestrator.AuthOrchestrator
import finn.request.auth.LogoutRequest
import finn.request.auth.OAuthLoginRequest
import finn.request.auth.ReIssueRequest
import finn.response.SuccessResponse
import finn.response.auth.ClientTokenResponse
import finn.service.TokenCookieService
import jakarta.servlet.http.HttpServletRequest
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
        // 4. web일 경우 쿠키에 토큰 set
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

    override fun reIssue(
        reIssueRequest: ReIssueRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<SuccessResponse<ClientTokenResponse>> {
        // 1. 리프레쉬 토큰 추출(앱/웹 여부에 따라 다르게 추출)
        val refreshToken =
            if (tokenCookieService.checkDeviceType(reIssueRequest.deviceType)) {
                tokenCookieService.getRefreshTokenInCookie(httpServletRequest)
            } else {
                reIssueRequest.refreshToken ?: throw InvalidTokenException("리프레쉬 토큰이 누락되었습니다.")
            }

        // 2. 리프레쉬 토큰 발급 후 리턴
        val response = authOrchestrator.reIssueToken(
            refreshToken,
            reIssueRequest.deviceType,
            reIssueRequest.deviceId
        )

        // 3. web일 경우 쿠키에 토큰 set
        if (tokenCookieService.checkDeviceType(reIssueRequest.deviceType)) {
            val cookie = tokenCookieService.setRefreshTokenInCookie(response.refreshToken)
            val responseForWeb = ClientTokenResponse(response.accessToken, null, response.deviceId)
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(SuccessResponse("200 Ok", "액세스/리프레쉬 토큰 재발급 성공", responseForWeb))
        }
        val responseForApp =
            ClientTokenResponse(response.accessToken, response.refreshToken, response.deviceId)
        return ResponseEntity.ok()
            .body(SuccessResponse("200 Ok", "액세스/리프레쉬 토큰 재발급 성공", responseForApp))
    }

    override fun logout(
        logoutRequest: LogoutRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<SuccessResponse<Nothing>> {
        val accessToken = "" // [TODO]: 인증 로직 구축되면 액세스 토큰 주입받아 사용
        authOrchestrator.logout(accessToken, logoutRequest.deviceId)

        return ResponseEntity.ok().body(SuccessResponse("200 Ok", "로그아웃 성공"))
    }
}