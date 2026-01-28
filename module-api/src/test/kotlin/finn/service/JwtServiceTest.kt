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
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class JwtServiceTest : DescribeSpec({

    // 1. 의존성 Mocking
    val jwtProvider = mockk<JwtProvider>()
    val jwtValidator = mockk<JwtValidator>()
    // Repository는 반환값이 없는(Unit) 메서드가 많으므로 relaxed=true로 설정하여 편의성 증대
    val userTokenRepository = mockk<UserTokenRepository>(relaxed = true)
    val userInfoRepository = mockk<UserInfoRepository>()
    val accessTokenValidity = 30 * 60 * 1000L // 30분

    // 2. 테스트 대상 주입
    val jwtService = JwtService(
        jwtProvider,
        jwtValidator,
        userTokenRepository,
        userInfoRepository,
        accessTokenValidity
    )

    // 3. Reflection 준비 (Private Field & Method 접근)
    val blackListField = JwtService::class.java.getDeclaredField("blackList").apply {
        isAccessible = true
    }
    val cleanupMethod = JwtService::class.java.getDeclaredMethod("cleanupExpiredTokens").apply {
        isAccessible = true
    }

    @Suppress("UNCHECKED_CAST")
    fun getBlackListMap(): ConcurrentHashMap<String, Long> {
        return blackListField.get(jwtService) as ConcurrentHashMap<String, Long>
    }

    describe("JwtService") {

        // ==========================================
        // 1. Issue (토큰 최초 발급)
        // ==========================================
        context("issue 메서드는") {
            val userId = UUID.randomUUID()
            val role = "MEMBER"
            val status = "ACTIVE"
            val deviceType = "APP"

            val mockAccessToken = "mock_access_token"
            val mockRefreshTokenString = "mock_refresh_token"
            val mockRefreshTokenObj = mockk<RefreshToken>()

            beforeEach {
                // Mock 설정
                every { jwtProvider.createAccessToken(userId, role, status) } returns mockAccessToken

                every { mockRefreshTokenObj.tokenValue } returns mockRefreshTokenString
                every { jwtProvider.createRefreshToken() } returns mockRefreshTokenObj
            }

            it("Access/Refresh 토큰을 생성하고 DB에 저장 후 결과를 반환한다") {
                // when
                val response = jwtService.issue(userId, role, status, deviceType)

                // then
                response.accessToken shouldBe mockAccessToken
                response.refreshToken shouldBe mockRefreshTokenString
                response.deviceId shouldNotBe null // 내부에서 랜덤 UUID 생성됨

                // Verify: DB 저장 호출 검증
                verify(exactly = 1) {
                    userTokenRepository.save(
                        userId,
                        any(), // newDeviceId (Random UUID)
                        deviceType,
                        mockRefreshTokenString
                    )
                }
            }
        }

        // ==========================================
        // 2. ReIssue (토큰 재발급 - Rotation & 탈취 감지)
        // ==========================================
        describe("reIssue 메서드는") {
            val requestRefreshToken = "client_refresh_token"
            val dbRefreshTokenValue = "client_refresh_token" // 정상 케이스
            val userId = UUID.randomUUID()
            val deviceId = UUID.randomUUID()
            val deviceType = "WEB"

            // Mocks
            val mockExtractedToken = mockk<RefreshToken>()
            val mockDbUserToken = mockk<UserToken>()
            val mockUserInfo = mockk<UserInfo>()

            // New Tokens
            val newAccessToken = "new_access_token"
            val newRefreshTokenString = "new_refresh_token"
            val mockNewRefreshTokenObj = mockk<RefreshToken>()
            val newDeviceId = UUID.randomUUID()

            beforeEach {
                clearMocks(jwtProvider, jwtValidator, userTokenRepository, userInfoRepository)

                // Common Setup
                every { mockExtractedToken.deviceId } returns deviceId
                every { jwtValidator.validateAndExtractRefreshToken(requestRefreshToken) } returns mockExtractedToken

                every { mockDbUserToken.refreshToken } returns dbRefreshTokenValue
                every { mockDbUserToken.userId } returns userId
                every { userTokenRepository.findByDeviceId(deviceId) } returns mockDbUserToken

                every { mockUserInfo.id } returns userId
                every { mockUserInfo.role.name } returns "MEMBER"
                every { mockUserInfo.status.name } returns "ACTIVE"
                every { userInfoRepository.findById(userId) } returns mockUserInfo

                every { jwtProvider.createAccessToken(userId, "MEMBER", "ACTIVE") } returns newAccessToken

                every { mockNewRefreshTokenObj.tokenValue } returns newRefreshTokenString
                every { mockNewRefreshTokenObj.deviceId } returns newDeviceId
                every { jwtProvider.createRefreshToken() } returns mockNewRefreshTokenObj
            }

            context("요청 토큰이 유효하고 DB 토큰과 일치하면 (정상)") {
                beforeEach {
                    every { jwtValidator.refreshTokenEquals(requestRefreshToken, dbRefreshTokenValue) } returns true
                }

                it("RTR(Rotation)을 수행하여 새 토큰들을 발급하고 DB를 업데이트한다") {
                    // when
                    val response = jwtService.reIssue(requestRefreshToken, userId, deviceType)

                    // then
                    response.accessToken shouldBe newAccessToken
                    response.refreshToken shouldBe newRefreshTokenString
                    response.deviceId shouldBe newDeviceId

                    // Verify: 업데이트 로직 호출 확인
                    verify(exactly = 1) {
                        userTokenRepository.updateRefreshToken(
                            requestRefreshToken,
                            newRefreshTokenString,
                            userId,
                            newDeviceId,
                            deviceType
                        )
                    }
                }
            }

            context("요청 토큰과 DB 토큰이 다르면 (탈취 시도)") {
                val stolenTokenValue = "stolen_token_value"

                beforeEach {
                    // DB에는 다른 값이 있다고 가정 (이미 Rotation됨)
                    every { mockDbUserToken.refreshToken } returns "other_value"
                    // Validator가 불일치 감지
                    every { jwtValidator.refreshTokenEquals(requestRefreshToken, "other_value") } returns false
                }

                it("TokenStolenRiskException을 던지고 해당 기기의 토큰 정보를 삭제한다") {
                    // when & then
                    shouldThrow<TokenStolenRiskException> {
                        jwtService.reIssue(requestRefreshToken, userId, deviceType)
                    }

                    // Verify: 즉시 삭제 로직 수행 확인
                    verify(exactly = 1) { userTokenRepository.deleteRefreshToken(deviceId) }
                    // Verify: 업데이트나 멤버 조회는 수행되지 않아야 함
                    verify(exactly = 0) { userInfoRepository.findById(any()) }
                }
            }

            context("입력된 토큰 자체가 유효하지 않으면 (JWT 검증 실패)") {
                beforeEach {
                    every {
                        jwtValidator.validateAndExtractRefreshToken(requestRefreshToken)
                    } throws InvalidTokenException("Invalid")
                }

                it("InvalidTokenException을 전파한다") {
                    shouldThrow<InvalidTokenException> {
                        jwtService.reIssue(requestRefreshToken, userId, deviceType)
                    }
                }
            }
        }

        // ==========================================
        // 3. Blacklist & Cleanup (Private Logic)
        // ==========================================
        describe("Blacklist 관리 로직") {
            beforeEach {
                getBlackListMap().clear()
            }

            context("addToBlacklist & isInBlackList") {
                val token = "blacklisted_access_token"

                it("토큰을 추가하면 맵에 저장되고, 조회 시 true를 반환한다") {
                    // when
                    jwtService.addToBlacklist(token)

                    // then
                    jwtService.isInBlackList(token) shouldBe true
                    getBlackListMap() shouldContainKey token
                }
            }

            context("cleanupExpiredTokens (@Scheduled)") {
                it("만료 시간이 지난 토큰만 맵에서 제거한다") {
                    // given
                    val map = getBlackListMap()
                    val now = System.currentTimeMillis()
                    val expiredToken = "expired"
                    val validToken = "valid"

                    // 테스트 데이터 주입
                    map[expiredToken] = now - 1000 // 1초 전 만료
                    map[validToken] = now + 10000  // 10초 후 만료

                    // when (Reflection Invoke)
                    cleanupMethod.invoke(jwtService)

                    // then
                    map shouldNotContainKey expiredToken
                    map shouldContainKey validToken
                }
            }
        }

        // ==========================================
        // 4. Release Refresh Token
        // ==========================================
        context("releaseRefreshToken 메서드는") {
            it("Repository의 delete 메서드를 호출한다") {
                val deviceId = UUID.randomUUID()

                jwtService.releaseRefreshToken(deviceId)

                verify(exactly = 1) { userTokenRepository.releaseRefreshToken(deviceId) }
            }
        }
    }
})