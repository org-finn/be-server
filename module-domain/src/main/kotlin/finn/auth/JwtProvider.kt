package finn.auth

import finn.entity.RefreshToken
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.access-token-validity}") private val accessTokenValidity: Long,
    @Value("\${jwt.refresh-token-validity}") private val refreshTokenValidity: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))

    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun createAccessToken(userInfoId: UUID, role: String, status: String): String {
        val now = Date()
        val validity = Date(now.time + accessTokenValidity)

        return Jwts.builder()
            .subject(userInfoId.toString())
            .claim("role", role)
            .claim("status", status)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun createRefreshToken(deviceId: UUID): RefreshToken {
        val now = Date()
        val validity = Date(now.time + refreshTokenValidity)

        val tokenValue = Jwts.builder()
            .claim("deviceId", deviceId.toString()) // 어떤 기기 토큰인지 식별용
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
        return RefreshToken.create(tokenValue, deviceId, now, validity)
    }




    // 토큰 만료 시간 가져오기 (쿠키 설정용)
    fun getRefreshTokenValidity(): Long = refreshTokenValidity
}