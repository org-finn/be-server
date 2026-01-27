package finn.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service

@Service
class TokenCookieService(
    @Value("\${jwt.refresh-token-validity}") private val refreshTokenValidity: Long
) {

    fun checkDeviceType(deviceType: String): Boolean = deviceType == "web"


    fun setRefreshTokenInCookie(refreshToken: String): ResponseCookie {
        return ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)    // JS에서 접근 불가능 (XSS 방지)
            .secure(true)      // HTTPS에서만 전송 (운영 환경 필수)
            .path("/")         // 모든 경로에서 쿠키 전송
            .maxAge(refreshTokenValidity)
            .sameSite("Strict") // CSRF 방지 (상황에 따라 'Lax' or 'None')
            .build()
    }
}