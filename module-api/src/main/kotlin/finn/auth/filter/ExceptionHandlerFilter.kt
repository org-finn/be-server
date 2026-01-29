package finn.config.security

import com.fasterxml.jackson.databind.ObjectMapper
import finn.exception.InvalidUserException
import finn.exception.auth.InvalidTokenException
import finn.exception.auth.TokenStolenRiskException
import finn.response.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import java.util.concurrent.TimeoutException

class ExceptionHandlerFilter(
    private val objectMapper: ObjectMapper
) : Filter {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpResponse = response as HttpServletResponse

        try {
            chain.doFilter(request, response)

        } catch (e: Exception) {
            handleException(httpResponse, e)
        }
    }

    private fun handleException(response: HttpServletResponse, e: Exception) {
        // 예외 타입에 따라 HTTP Status, 커스텀 코드, 메시지 정의
        val (status, errorCode, message) = when (e) {
            // 1. [401] 기본 커스텀 에러
            is InvalidTokenException -> Triple(
                HttpServletResponse.SC_UNAUTHORIZED,
                e.code.code,
                e.message!!
            )

            // 2. [401] 기타 발생가능한 예외
            is SignatureException, is MalformedJwtException, is JwtException -> Triple(
                HttpServletResponse.SC_UNAUTHORIZED,
                "401 Unauthorized",
                "토큰 유효성 검증에 실패했습니다."
            )

            // 3. [403] 토큰 탈취 감지 (사용자 정의 예외)
            is TokenStolenRiskException -> Triple(
                HttpServletResponse.SC_FORBIDDEN,
                "403 Forbidden",
                "토큰 탈취 위험이 감지되어 로그아웃을 실행합니다."
            )

            // 4. [408] 타임아웃 (예: 외부 API 호출 지연 등)
            is TimeoutException -> Triple(
                HttpServletResponse.SC_REQUEST_TIMEOUT,
                "408 Timeout",
                "요청 시간이 초과되었습니다."
            )

            // 5. [400] 잘못된 요청 (필터 단계에서의 파라미터 오류 등)
            is IllegalArgumentException -> Triple(
                HttpServletResponse.SC_BAD_REQUEST,
                "400 Bad Request",
                e.message ?: "잘못된 요청입니다."
            )

            // 6. [401] 가입이 완료되지 않았거나 탈퇴한 회원
            is InvalidUserException -> Triple(
                HttpServletResponse.SC_UNAUTHORIZED,
                e.code.code,
                e.message!!
            )

            // 7. [500] 나머지 알 수 없는 서버 에러
            else -> {
                e.printStackTrace() // 로그 남기기
                Triple(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "500 Internal Server Error",
                    "서버 내부 오류가 발생했습니다."
                )
            }
        }

        // 응답 생성
        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = ErrorResponse(errorCode, message)
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}