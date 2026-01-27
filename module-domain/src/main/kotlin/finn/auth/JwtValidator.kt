package finn.auth

import finn.entity.AccessToken
import finn.entity.RefreshToken
import finn.exception.auth.InvalidTokenException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtValidator(
    @Value("\${jwt.secret}") private val secretKey: String,
) {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun refreshTokenEquals(userRefreshToken: String, dbRefreshToken: String?): Boolean {
        return userRefreshToken == dbRefreshToken
    }

    private val key: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))

    fun validateAndExtractAccessToken(token: String): AccessToken {
        return try {
            val claims = getClaims(token)
            AccessToken.create(
                subject = claims.subject,
                role = claims["role"].toString(),
                status = claims["status"].toString(),
                issuedAt = claims.issuedAt,
                expiresAt = claims.expiration
            )
        } catch (e: Exception) {
            log.error { "jwt validation error: ${e.message}" }
            throw InvalidTokenException("만료되었거나 유효하지 않은 토큰입니다.")
        }
    }

    fun validateAndExtractRefreshToken(token: String): RefreshToken {
        return try {
            val claims = getClaims(token)
            RefreshToken.create(
                tokenValue = token,
                deviceId = UUID.fromString(claims["deviceId"].toString()),
                issuedAt = claims.issuedAt,
                expiredAt = claims.expiration
            )
        } catch (e: Exception) {
            log.error { "jwt validation error: ${e.message}" }
            throw InvalidTokenException("만료되었거나 유효하지 않은 토큰입니다.")
        }
    }

    // 내부 유틸: Claims 파싱
    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}