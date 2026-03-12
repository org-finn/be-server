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
        "/api/v1/login/**",              // 소셜 로그인 관련
        "/api/v1/reIssue",               // 토큰 재발급
        "/swagger-ui/**",                // API 문서
        "/v3/api-docs/**",               // API 문서
        "/api/v1/prediction/**",         // 종목 예측 관련 (전체 허용)
        "/api/v1/article-summary/**",    // 뉴스 요약 관련 (전체 허용)
        "/api/v1/article",
        "/api/v1/article/**",            // 아티클 조회 및 티커 리스트 (전체 허용)
        "/api/v1/search-preview/**",      // 종목 검색 자동완성
        "/api/v1/market-status/**",       // 금일 장 정보
        "/api/v1/exchange-rate/**",       // 실시간 환율
        "/api/v1/join/**",                // 회원 가입 시 필요한 종목 리스트 (추가됨)
        "/api/v1/price/ticker/*/graph",   // 과거 주가 그래프만 허용 (real-time 경로는 제외 처리)
        "/api/admin/**"                   // 관리자 API(별도 키로 인증)
    )

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val requestUri = httpRequest.requestURI

        if ("OPTIONS" == httpRequest.method) {
            chain.doFilter(request, response)
            return
        }

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