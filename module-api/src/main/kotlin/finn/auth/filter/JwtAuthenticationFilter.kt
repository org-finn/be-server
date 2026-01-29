package finn.auth.filter

import finn.auth.JwtValidator
import finn.exception.InvalidUserException
import finn.exception.auth.InvalidTokenException
import finn.service.JwtService
import io.jsonwebtoken.JwtException
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.util.PatternMatchUtils

class JwtAuthenticationFilter(
    private val jwtValidator: JwtValidator,
    private val jwtService: JwtService,
) : Filter {
    // 인증 없이 통과시킬 URL 패턴 (whitelist)
    private val whiteList = arrayOf(
        "/api/v1/login/*",
        "/api/v1/reIssue",
        "/swagger-ui/*",
        "/v3/api-docs/*",
        "/api/v1/prediction/*",
        "/api/v1/article-summary/*",
        "/api/v1/price/*",
        "/api/v1/article/*",
        "/api/v1/search-preview/*",
        "/api/v1/market-status/*",
        "/api/v1/exchange-rate/*",
    )

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val requestUri = httpRequest.requestURI

        // 1. 토큰 추출 시도 (가장 먼저 수행)
        val token = resolveToken(httpRequest)

        // 2. 토큰이 존재하는 경우 -> 경로와 상관없이 "무조건" 검증 및 주입
        if (token != null) {
            // A. 블랙리스트 검사
            if (jwtService.isInBlackList(token)) {
                throw JwtException("이미 로그아웃된 사용자입니다.")
            }

            // B. 유효성 검증 (만료 여부 등)
            val accessToken = jwtValidator.validateAndExtractAccessToken(token)

            // C. 미가입/탈퇴 상태 검증
            if (accessToken.status != "REGISTERED") {
                throw InvalidUserException("가입을 완료하지 않거나 탈퇴한 사용자입니다.")
            }

            // D. userId 추출 및 세팅
            // (Resolver에서 UUID 타입을 기대한다면 여기서 변환해주는 것이 좋습니다)
            val userId = accessToken.subject
            httpRequest.setAttribute("userId", userId)
        }
        // 3. 토큰이 없는 경우 -> 화이트리스트 체크
        else {
            // 토큰도 없고, 화이트리스트 경로도 아니라면 -> 인증 필수 에러
            if (!isWhiteList(requestUri)) {
                throw InvalidTokenException("토큰이 누락되었습니다.")
            }
            // 화이트리스트 경로라면 -> 그냥 통과 (Attribute에 userId 없음 -> Resolver가 null 처리)
        }

        // 4. 다음 로직 수행
        chain.doFilter(request, response)
    }

    private fun isWhiteList(requestUri: String): Boolean {
        return PatternMatchUtils.simpleMatch(whiteList, requestUri)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }

}