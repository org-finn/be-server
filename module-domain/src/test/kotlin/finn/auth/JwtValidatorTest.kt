package finn.auth

import finn.entity.UserRole
import finn.entity.UserStatus
import finn.exception.auth.InvalidTokenException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.util.*
import javax.crypto.SecretKey

class JwtValidatorTest : DescribeSpec({

    // 테스트용 키 및 Validator 초기화
    val testSecretKey: SecretKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256)
    val encodedSecretKey = Base64.getEncoder().encodeToString(testSecretKey.encoded)

    val jwtValidator = JwtValidator(encodedSecretKey)

    describe("JwtValidator") {

        context("refreshTokenEquals 메서드는") {
            it("두 토큰 문자열이 일치하면 true를 반환한다") {
                jwtValidator.refreshTokenEquals("abc", "abc").shouldBeTrue()
            }

            it("두 토큰 문자열이 다르면 false를 반환한다") {
                jwtValidator.refreshTokenEquals("abc", "xyz").shouldBeFalse()
            }
        }

        describe("validateAndExtractToken 메서드는") {

            context("유효한 액세스 토큰이 주어지면") {
                val subject = UUID.randomUUID()
                val role = UserRole.USER.name
                val status = UserStatus.REGISTERED.name
                val now = Date()
                val validity = Date(now.time + 100000)

                val validToken = Jwts.builder()
                    .subject(subject.toString())
                    .claim("role", role)
                    .claim("status", status)
                    .issuedAt(now)
                    .expiration(validity)
                    .signWith(testSecretKey)
                    .compact()

                it("파싱하여 올바른 AccessToken 객체를 반환한다") {
                    val result = jwtValidator.validateAndExtractAccessToken(validToken)

                    result.subject.toString() shouldBe subject.toString()
                    result.role shouldBe role
                    result.status shouldBe status
                }
            }

            context("유효한 리프레쉬 토큰이 주어지면") {
                val deviceId = UUID.randomUUID()
                val now = Date()
                val validity = Date(now.time + 100000)

                val validToken = Jwts.builder()
                    .claim("deviceId", deviceId.toString())
                    .issuedAt(now)
                    .expiration(validity)
                    .signWith(testSecretKey)
                    .compact()

                it("파싱하여 올바른 AccessToken 객체를 반환한다") {
                    val result = jwtValidator.validateAndExtractRefreshToken(validToken)
                }
            }

            context("만료된 토큰이 주어지면") {
                val pastDate = Date(System.currentTimeMillis() - 1000) // 1초 전 만료
                val expiredToken = Jwts.builder()
                    .subject("expiredUser")
                    .expiration(pastDate)
                    .signWith(testSecretKey)
                    .compact()

                it("InvalidTokenException을 던진다") {
                    val exception = shouldThrow<InvalidTokenException> {
                        jwtValidator.validateAndExtractAccessToken(expiredToken)
                    }
                    exception.message shouldBe "만료되었거나 유효하지 않은 토큰입니다."
                }
            }

            context("서명이 위조된(다른 키로 서명된) 토큰이 주어지면") {
                val hackerKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256)
                val forgedToken = Jwts.builder()
                    .subject("hacker")
                    .signWith(hackerKey)
                    .compact()

                it("InvalidTokenException을 던진다") {
                    shouldThrow<InvalidTokenException> {
                        jwtValidator.validateAndExtractAccessToken(forgedToken)
                    }
                }
            }

            context("잘못된 형식의 문자열이 주어지면") {
                val garbageToken = "this.is.not.jwt"

                it("InvalidTokenException을 던진다") {
                    shouldThrow<InvalidTokenException> {
                        jwtValidator.validateAndExtractAccessToken(garbageToken)
                    }
                }
            }

            context("필수 정보(Claims)가 누락된 토큰이 주어지면") {
                val simpleToken = Jwts.builder()
                    .subject(UUID.randomUUID().toString())
                    .signWith(testSecretKey)
                    .compact()

                it("InvalidTokenException을 던진다") {
                    shouldThrow<InvalidTokenException> {
                        jwtValidator.validateAndExtractAccessToken(simpleToken)
                    }
                }
            }
        }
    }
})