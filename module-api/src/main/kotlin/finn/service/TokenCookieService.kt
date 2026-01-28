package finn.service

import finn.exception.auth.InvalidTokenException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service

@Service
class TokenCookieService(
    @Value("\${jwt.refresh-token-validity}") private val refreshTokenValidity: Long
) {
    companion object {
        private val REFRESH_TOKEN_NAME = "articker_refresh_token"
    }

    fun checkDeviceType(deviceType: String): Boolean = deviceType == "web"


    fun setRefreshTokenInCookie(refreshToken: String): ResponseCookie {
        return ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken)
            .httpOnly(true)    // JS에서 접근 불가능 (XSS 방지)
            .secure(true)      // HTTPS에서만 전송 (운영 환경 필수)
            .path("/")         // 모든 경로에서 쿠키 전송
            .maxAge(refreshTokenValidity)
            .sameSite("Strict") // CSRF 방지 (상황에 따라 'Lax' or 'None')
            .build()
    }

    fun getRefreshTokenInCookie(request: HttpServletRequest): String {
        return request.cookies
            ?.find { it.name == REFRESH_TOKEN_NAME }
            ?.value
            ?: throw InvalidTokenException("리프레쉬 토큰이 누락되었습니다.")
    }
}