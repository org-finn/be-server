package finn.service

import finn.auth.JwtProvider
import finn.auth.JwtValidator
import finn.exception.auth.InvalidTokenException
import finn.repository.UserInfoRepository
import finn.repository.UserTokenRepository
import finn.response.auth.TokenResponse
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class JwtService(
    private val jwtProvider: JwtProvider,
    private val jwtValidator: JwtValidator,
    private val userTokenRepository: UserTokenRepository,
    private val userInfoRepository: UserInfoRepository
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun issue(userId: UUID, role: String, status: String, deviceType: String): TokenResponse {
        val accessToken = jwtProvider.createAccessToken(userId, role, status)
        val newDeviceId = UUID.randomUUID()
        val refreshToken = jwtProvider.createRefreshToken(newDeviceId)
        userTokenRepository.save(
            userId, newDeviceId, deviceType, refreshToken.tokenValue,
            refreshToken.expiredAt, refreshToken.issuedAt
        )
        return TokenResponse(accessToken, refreshToken.tokenValue)
    }

    fun reissue(userRefreshTokenString: String): TokenResponse {
        // 1. Refresh Token 자체의 유효성 검증 (위변조/만료 체크 - JWT 레벨)
        val refreshToken = jwtValidator.validateAndExtractRefreshToken(userRefreshTokenString)
        val deviceId = refreshToken.deviceId

        // 2. DB에서 device_id로 토큰 정보 조회
        val dbRefreshToken = userTokenRepository.findByDeviceId(deviceId)

        // 3. 토큰 일치 여부 검증 (DB값 vs 쿠키값) -> 탈취 감지
        if (!jwtValidator.refreshTokenEquals(userRefreshTokenString, dbRefreshToken.refreshToken)) {
            log.error { "db에 저장된 리프레쉬 토큰 값과 일치하지 않습니다. 탈취 가능성 있음" }
            userTokenRepository.deleteRefreshToken(deviceId)
            throw InvalidTokenException("유효하지 않거나 만료된 토큰입니다.")
        }

        // 4. 멤버 조회
        val userInfo = userInfoRepository.findById(dbRefreshToken.userId)

        // 5. 새 토큰 발급 (Rotation)
        val newAccessToken =
            jwtProvider.createAccessToken(userInfo.id, userInfo.role.name, userInfo.status.name)
        val newRefreshToken = jwtProvider.createRefreshToken(deviceId)

        // 6. DB 업데이트 (Rotation 수행)
        userTokenRepository.updateRefreshToken(newRefreshToken.tokenValue, deviceId)
        return TokenResponse(newAccessToken, newRefreshToken.tokenValue)
    }
}