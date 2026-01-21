package finn.auth

import finn.entity.UserRole
import finn.entity.UserStatus
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.*
import javax.crypto.SecretKey

class JwtProviderTest : DescribeSpec({

    // 테스트 환경 설정 (Fixture)
    val accessTokenValidity = 1000L * 60 * 30 // 30분
    val refreshTokenValidity = 1000L * 60 * 60 * 24 * 7 // 7일

    // 테스트용 키 생성 (HS256)
    val testSecretKey: SecretKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256)
    // 운영 코드가 Base64 디코딩을 수행하므로, 주입할 때는 인코딩된 문자열을 넘김
    val encodedSecretKey = Base64.getEncoder().encodeToString(testSecretKey.encoded)

    val jwtProvider = JwtProvider(
        secretKey = encodedSecretKey,
        accessTokenValidity = accessTokenValidity,
        refreshTokenValidity = refreshTokenValidity
    )

    describe("JwtProvider") {

        context("createAccessToken 메서드는") {
            val userInfoId = UUID.randomUUID()
            val role = UserRole.USER.name
            val status = UserStatus.REGISTERED.name

            it("Subject, Role, Status가 포함된 유효한 토큰을 생성해야 한다") {
                // when
                val token = jwtProvider.createAccessToken(userInfoId, role, status)

                // then
                token.shouldNotBeNull()

                // 생성된 토큰 검증 (복호화)
                val claims = Jwts.parser()
                    .verifyWith(testSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload

                claims.subject shouldBe userInfoId.toString()
                claims["role"] shouldBe role
                claims["status"] shouldBe status

                // 만료 시간 검증 (오차범위 1초)
                val expirationDiff = claims.expiration.time - Date().time
                expirationDiff shouldBeLessThan (accessTokenValidity + 1000)
            }
        }

        context("createRefreshToken 메서드는") {
            val deviceId = UUID.randomUUID()

            it("deviceId 정보가 포함된 유효한 토큰을 생성해야 한다") {
                // when
                val token = jwtProvider.createRefreshToken(deviceId)

                // then
                token.shouldNotBeNull()

                // 생성된 토큰 검증
                val claims = Jwts.parser()
                    .verifyWith(testSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload

                claims["deviceId"] shouldBe deviceId.toString()

                // 만료 시간 검증
                val expirationDiff = claims.expiration.time - Date().time
                expirationDiff shouldBeLessThan (refreshTokenValidity + 1000)
            }
        }

        context("getRefreshTokenValidity 메서드는") {
            it("설정된 유효 기간을 정확히 반환해야 한다") {
                jwtProvider.getRefreshTokenValidity() shouldBe refreshTokenValidity
            }
        }
    }
})