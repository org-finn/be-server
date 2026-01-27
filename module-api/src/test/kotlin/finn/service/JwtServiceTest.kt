package finn.service

import finn.auth.JwtProvider
import finn.auth.JwtValidator
import finn.entity.RefreshToken
import finn.entity.UserInfo
import finn.entity.UserToken
import finn.exception.auth.InvalidTokenException
import finn.exception.auth.TokenStolenRiskException
import finn.repository.UserInfoRepository
import finn.repository.UserTokenRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*

class JwtServiceTest : DescribeSpec({

    // 1. 의존성 Mocking
    val jwtProvider = mockk<JwtProvider>()
    val jwtValidator = mockk<JwtValidator>()
    val userTokenRepository = mockk<UserTokenRepository>(relaxed = true)
    val userInfoRepository = mockk<UserInfoRepository>()

    // 2. 테스트 대상 주입
    val jwtService = JwtService(
        jwtProvider,
        jwtValidator,
        userTokenRepository,
        userInfoRepository
    )

    describe("JwtService") {

        // =======================
        // 1. issue (토큰 발급) 테스트
        // =======================
        context("issue 메서드는") {
            val userId = UUID.randomUUID()
            val role = "MEMBER"
            val status = "ACTIVE"
            val deviceType = "WEB"

            val mockAccessToken = "mock_access_token_string"
            val mockRefreshTokenObj = mockk<RefreshToken>()
            val mockRtValue = "mock_refresh_token_string"
            val mockIssuedAt = Date()
            val mockExpiredAt = Date(System.currentTimeMillis() + 100000)

            beforeEach {
                every { mockRefreshTokenObj.tokenValue } returns mockRtValue
                every { mockRefreshTokenObj.issuedAt } returns mockIssuedAt
                every { mockRefreshTokenObj.expiredAt } returns mockExpiredAt
            }

            it("Access/Refresh 토큰을 생성하고 DB 저장 후, 새로운 deviceId를 포함한 응답을 반환해야 한다") {
                // given
                every { jwtProvider.createAccessToken(userId, role, status) } returns mockAccessToken
                every { jwtProvider.createRefreshToken() } returns mockRefreshTokenObj

                // when
                val response = jwtService.issue(userId, role, status, deviceType)

                // then
                response.accessToken shouldBe mockAccessToken
                response.refreshToken shouldBe mockRtValue
                response.deviceId shouldNotBe null // 랜덤 UUID 생성 확인

                // Verify: DB 저장 메서드 호출 검증
                verify(exactly = 1) {
                    userTokenRepository.save(
                        userId = userId,
                        deviceId = any(), // 내부 랜덤 생성 UUID
                        deviceType = deviceType,
                        tokenValue = mockRtValue,
                        expiredAt = mockExpiredAt,
                        issuedAt = mockIssuedAt
                    )
                }
            }
        }

        // =======================
        // 2. reissue (토큰 재발급) 테스트
        // =======================
        describe("reissue 메서드는") {
            val userRefreshTokenString = "user_submitted_refresh_token"
            val dbRefreshTokenValue = "user_submitted_refresh_token"
            val deviceId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val deviceType = "WEB"

            // Mock Objects
            val mockExtractedToken = mockk<RefreshToken>()
            val mockDbTokenEntity = mockk<UserToken>()
            val mockUserInfo = mockk<UserInfo>()

            // New Tokens
            val mockNewAccessToken = "new_access_token"
            val mockNewRefreshTokenObj = mockk<RefreshToken>()
            val mockNewRefreshTokenValue = "new_refresh_token_value"
            val mockNewIssuedAt = Date()
            val mockNewExpiredAt = Date(System.currentTimeMillis() + 100000)

            beforeEach {
                clearMocks(jwtProvider, jwtValidator, userTokenRepository, userInfoRepository)

                // 1. Validator & Repo Mocks
                every { jwtValidator.validateAndExtractRefreshToken(userRefreshTokenString) } returns mockExtractedToken
                every { mockDbTokenEntity.refreshToken } returns dbRefreshTokenValue
                every { mockDbTokenEntity.userId } returns userId
                every { userTokenRepository.findByDeviceId(deviceId) } returns mockDbTokenEntity
                every { jwtValidator.refreshTokenEquals(any(), any()) } returns true

                // 2. UserInfo Mock
                every { mockUserInfo.id } returns userId
                every { mockUserInfo.role.name } returns "MEMBER"
                every { mockUserInfo.status.name } returns "ACTIVE"
                every { userInfoRepository.findById(userId) } returns mockUserInfo

                // 3. Provider Mock (새 토큰 생성)
                every { mockNewRefreshTokenObj.tokenValue } returns mockNewRefreshTokenValue
                every { mockNewRefreshTokenObj.issuedAt } returns mockNewIssuedAt
                every { mockNewRefreshTokenObj.expiredAt } returns mockNewExpiredAt

                every { jwtProvider.createAccessToken(any(), any(), any()) } returns mockNewAccessToken
                every { jwtProvider.createRefreshToken() } returns mockNewRefreshTokenObj
            }

            context("정상적인 토큰이고, DB 업데이트(Rotation)가 성공하면") {
                it("기존 deviceId를 유지하며 새 토큰을 반환한다") {
                    // given
                    every { userTokenRepository.updateRefreshToken(mockNewRefreshTokenValue, deviceId) } returns true

                    // when
                    val response = jwtService.reIssue(userRefreshTokenString, deviceId, deviceType)

                    // then
                    response.accessToken shouldBe mockNewAccessToken
                    response.refreshToken shouldBe mockNewRefreshTokenValue
                    response.deviceId shouldBe deviceId // 기존 ID 유지

                    verify(exactly = 1) { userTokenRepository.updateRefreshToken(mockNewRefreshTokenValue, deviceId) }
                    verify(exactly = 0) { userTokenRepository.save(any(), any(), any(), any(), any(), any()) }
                }
            }

            context("정상적인 토큰이지만, DB 업데이트가 실패하면 (일치하는 deviceId 없음)") {
                it("새로운 deviceId를 생성하여 DB에 저장(save)하고, 새 deviceId를 반환한다") {
                    // given
                    // 업데이트 실패 시뮬레이션
                    every { userTokenRepository.updateRefreshToken(mockNewRefreshTokenValue, deviceId) } returns false

                    // when
                    val response = jwtService.reIssue(userRefreshTokenString, deviceId, deviceType)

                    // then
                    response.accessToken shouldBe mockNewAccessToken
                    response.refreshToken shouldBe mockNewRefreshTokenValue
                    response.deviceId shouldNotBe deviceId // 새로운 ID 발급 확인

                    // Verify: update 실패 후 save 호출 확인
                    verify(exactly = 1) { userTokenRepository.updateRefreshToken(mockNewRefreshTokenValue, deviceId) }
                    verify(exactly = 1) {
                        userTokenRepository.save(
                            userId = userId,
                            deviceId = any(), // 새 ID
                            deviceType = deviceType,
                            tokenValue = mockNewRefreshTokenValue,
                            expiredAt = mockNewExpiredAt,
                            issuedAt = mockNewIssuedAt
                        )
                    }
                }
            }

            context("DB 토큰 값과 요청 토큰 값이 일치하지 않으면 (토큰 탈취 감지)") {
                it("해당 deviceId 데이터를 삭제하고 TokenStolenRiskException을 던진다") {
                    // given
                    val differentDbTokenValue = "different_db_token"
                    every { mockDbTokenEntity.refreshToken } returns differentDbTokenValue
                    every { jwtValidator.refreshTokenEquals(userRefreshTokenString, differentDbTokenValue) } returns false

                    // when & then
                    shouldThrow<TokenStolenRiskException> {
                        jwtService.reIssue(userRefreshTokenString, deviceId, deviceType)
                    }

                    // Verify: 삭제 로직 실행 확인
                    verify(exactly = 1) { userTokenRepository.deleteRefreshToken(deviceId) }
                    verify(exactly = 0) { userTokenRepository.updateRefreshToken(any(), any()) }
                }
            }

            context("Validator 검증 단계에서 예외가 발생하면") {
                it("Service도 예외를 던진다") {
                    // given
                    every {
                        jwtValidator.validateAndExtractRefreshToken(userRefreshTokenString)
                    } throws InvalidTokenException("Invalid Token")

                    // when & then
                    shouldThrow<InvalidTokenException> {
                        jwtService.reIssue(userRefreshTokenString, deviceId, deviceType)
                    }
                }
            }
        }
    }
})