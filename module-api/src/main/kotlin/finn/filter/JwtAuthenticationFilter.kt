package finn.filter

import finn.auth.JwtValidator
import finn.exception.InvalidUserException
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

        // 1. 화이트리스트 검사 (인증 불필요한 요청은 통과)
        if (isWhiteList(requestUri)) {
            chain.doFilter(request, response)
            return
        }

        // 2. 토큰 추출
        val token = resolveToken(httpRequest)
            ?: throw JwtException("토큰이 존재하지 않습니다.")

        // 3. 블랙리스트 검사
        if (jwtService.isInBlackList(token)) {
            throw JwtException("이미 로그아웃된 사용자입니다.")
        }

        // 4. 유효성 검증 (만료 여부 등)
        val accessToken = jwtValidator.validateAndExtractAccessToken(token)

        // 5. 미가입/탈퇴 상태 검증
        if (accessToken.status != "REGISTERED") {
            throw InvalidUserException("가입을 완료하지 않거나 탈퇴한 사용자입니다.")
        }

        // 6. userId 추출 및 세팅
        val userId = accessToken.subject
        httpRequest.setAttribute("userId", userId)

        // 7. 다음 로직 수행
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