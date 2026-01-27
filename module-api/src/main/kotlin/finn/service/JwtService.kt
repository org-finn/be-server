package finn.service

import finn.auth.JwtProvider
import finn.auth.JwtValidator
import finn.exception.auth.TokenStolenRiskException
import finn.repository.UserInfoRepository
import finn.repository.UserTokenRepository
import finn.response.auth.TokenResponse
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
@ExposedTransactional(readOnly = true)
class JwtService(
    private val jwtProvider: JwtProvider,
    private val jwtValidator: JwtValidator,
    private val userTokenRepository: UserTokenRepository,
    private val userInfoRepository: UserInfoRepository,
    @Value("\${jwt.access-token-validity}") private val ACCESS_TOKEN_VALIDITY: Long
) {
    private val blackList = ConcurrentHashMap<String, Long>() // key: accessToken, value: validity

    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun issue(userId: UUID, role: String, status: String, deviceType: String): TokenResponse {
        val accessToken = jwtProvider.createAccessToken(userId, role, status)
        val newDeviceId = UUID.randomUUID()
        val refreshToken = jwtProvider.createRefreshToken()
        userTokenRepository.save(
            userId, newDeviceId, deviceType, refreshToken.tokenValue,
            refreshToken.expiredAt, refreshToken.issuedAt
        )
        return TokenResponse(accessToken, refreshToken.tokenValue, newDeviceId)
    }

    fun reIssue(userRefreshTokenString: String, deviceId: UUID, deviceType: String): TokenResponse {
        // 1. Refresh Token 자체의 유효성 검증 (위변조/만료 체크 - JWT 레벨)
        jwtValidator.validateAndExtractRefreshToken(userRefreshTokenString)

        // 2. DB에서 device_id로 토큰 정보 조회
        val dbRefreshToken = userTokenRepository.findByDeviceId(deviceId)

        // 3. 토큰 일치 여부 검증 (DB값 vs 쿠키값) -> 탈취 감지
        if (!jwtValidator.refreshTokenEquals(userRefreshTokenString, dbRefreshToken.refreshToken)) {
            log.error { "db에 저장된 리프레쉬 토큰 값과 일치하지 않습니다. 탈취 가능성 있음" }
            userTokenRepository.deleteRefreshToken(deviceId)
            throw TokenStolenRiskException("유효하지 않거나 만료된 토큰입니다.")
        }

        // 4. 멤버 조회
        val userInfo = userInfoRepository.findById(dbRefreshToken.userId)

        // 5. 새 토큰 발급
        val newAccessToken =
            jwtProvider.createAccessToken(userInfo.id, userInfo.role.name, userInfo.status.name)
        val newRefreshToken = jwtProvider.createRefreshToken()

        // 6. DB 업데이트(만약 일치하는 device_id가 없다면 user_token을 새로 발급하여 리턴)
        if (!userTokenRepository.updateRefreshToken(
                newRefreshToken.tokenValue,
                deviceId,
                newRefreshToken.issuedAt,
                newRefreshToken.expiredAt
            )
        ) {
            val newDeviceId = UUID.randomUUID()
            userTokenRepository.save(
                userInfo.id, newDeviceId, deviceType, newRefreshToken.tokenValue,
                newRefreshToken.expiredAt, newRefreshToken.issuedAt
            )
            return TokenResponse(newAccessToken, newRefreshToken.tokenValue, newDeviceId)
        }
        return TokenResponse(newAccessToken, newRefreshToken.tokenValue, deviceId)
    }

    fun releaseRefreshToken(deviceId: UUID) {
        userTokenRepository.releaseRefreshToken(deviceId)
    }


    fun addToBlacklist(token: String) {
        blackList[token] = System.currentTimeMillis().plus(ACCESS_TOKEN_VALIDITY)
    }

    fun isInBlackList(token: String): Boolean {
        return blackList.containsKey(token)
    }


    @Scheduled(fixedRate = 60000) // 1분마다 실행
    private fun cleanupExpiredTokens() {
        val now = System.currentTimeMillis()

        // entrySet을 순회하며 만료된 요소 제거
        blackList.entries.removeIf { entry ->
            entry.value < now
        }
    }
}