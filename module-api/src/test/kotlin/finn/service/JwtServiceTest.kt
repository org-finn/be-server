package finn.service

import finn.auth.JwtProvider
import finn.auth.JwtValidator
import finn.entity.Token
import finn.entity.UserInfo
import finn.entity.UserRole
import finn.entity.UserStatus
import finn.exception.auth.InvalidTokenException
import finn.repository.UserInfoRepository
import finn.repository.UserTokenRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*

class JwtServiceTest : DescribeSpec({

    // 1. 의존성 Mocking
    val jwtProvider = mockk<JwtProvider>()
    val jwtValidator = mockk<JwtValidator>()
    val userTokenRepository = mockk<UserTokenRepository>(relaxed = true) // void 메서드 호출 허용
    val userInfoRepository = mockk<UserInfoRepository>()

    // 2. 테스트 대상 주입
    val jwtService = JwtService(
        jwtProvider,
        jwtValidator,
        userTokenRepository,
        userInfoRepository
    )

    describe("JwtService") {

        context("issue 메서드는") {
            val userId = UUID.randomUUID()
            val role = UserRole.USER
            val status = UserStatus.REGISTERED
            val mockAccessToken = "mock_access_token"
            val mockRefreshToken = "mock_refresh_token"

            it("Access 토큰과 Refresh 토큰을 생성하여 반환해야 한다") {
                // given
                every { jwtProvider.createAccessToken(userId, role.name, status.name) } returns mockAccessToken
                every { jwtProvider.createRefreshToken(any()) } returns mockRefreshToken

                // when
                val response = jwtService.issue(userId, role.name, status.name)

                // then
                response.accessToken shouldBe mockAccessToken
                response.refreshToken shouldBe mockRefreshToken

                // provider 호출 검증
                verify(exactly = 1) { jwtProvider.createAccessToken(userId, role.name, status.name) }
                verify(exactly = 1) { jwtProvider.createRefreshToken(any()) }
            }
        }

        describe("reissue 메서드는") {
            val oldRefreshTokenString = "old_refresh_token"
            val dbRefreshTokenString = "old_refresh_token"
            val deviceId = UUID.randomUUID()
            val userId = UUID.randomUUID()

            // Mock 객체 준비
            val mockToken = mockk<Token>()
            val mockUserInfo = mockk<UserInfo>()

            beforeEach {
                clearMocks(jwtProvider, jwtValidator, userTokenRepository, userInfoRepository)

                // 공통적인 Mock 동작 정의 (기본값: 성공 시나리오)
                every { mockToken.deviceId } returns deviceId
                every { mockToken.subject } returns userId
                every { jwtValidator.validateAndExtractToken(oldRefreshTokenString) } returns mockToken
                every { userTokenRepository.findByDeviceId(deviceId) } returns dbRefreshTokenString
                every { jwtValidator.refreshTokenEquals(oldRefreshTokenString, dbRefreshTokenString) } returns true

                // UserInfo Mock 설정 (Role, Status Enum 가정)
                every { mockUserInfo.id } returns userId
                every { mockUserInfo.role.name } returns UserRole.USER.name
                every { mockUserInfo.status.name } returns UserStatus.REGISTERED.name
                every { userInfoRepository.findById(userId) } returns mockUserInfo

                // Provider Mock
                every { jwtProvider.createAccessToken(any(), any(), any()) } returns "new_access_token"
                every { jwtProvider.createRefreshToken(any()) } returns "new_refresh_token"
            }

            context("유효한 Refresh Token이고 DB 값과 일치하면") {
                it("토큰을 Rotation(재발급 및 DB 업데이트)하고 새 토큰을 반환한다") {
                    // when
                    val response = jwtService.reissue(oldRefreshTokenString)

                    // then
                    response.accessToken shouldBe "new_access_token"
                    response.refreshToken shouldBe "new_refresh_token"

                    // DB 업데이트가 호출되었는지 검증 (Rotation)
                    verify(exactly = 1) {
                        userTokenRepository.updateRefreshToken("new_refresh_token", deviceId)
                    }
                    // 삭제 로직은 호출되지 않아야 함
                    verify(exactly = 0) { userTokenRepository.deleteRefreshToken(any()) }
                }
            }

            context("Refresh Token 내부에 deviceId가 없으면") {
                it("InvalidTokenException을 던진다") {
                    // given
                    every { mockToken.deviceId } returns null // deviceId 누락 상황

                    // when & then
                    shouldThrow<InvalidTokenException> {
                        jwtService.reissue(oldRefreshTokenString)
                    }
                }
            }

            context("DB에 저장된 토큰 값과 요청받은 토큰 값이 일치하지 않으면 (토큰 탈취 시도 가능성)") {
                it("해당 기기의 Refresh Token을 DB에서 삭제하고 예외를 던진다") {
                    // given
                    // DB에는 다른 값이 저장되어 있음
                    every { jwtValidator.refreshTokenEquals(oldRefreshTokenString, dbRefreshTokenString) } returns false

                    // when & then
                    shouldThrow<InvalidTokenException> {
                        jwtService.reissue(oldRefreshTokenString)
                    }

                    // 핵심 검증: 탈취 감지 시 삭제 로직이 실행되었는가?
                    verify(exactly = 1) { userTokenRepository.deleteRefreshToken(deviceId) }

                    // DB 업데이트나 토큰 생성은 실행되지 않아야 함
                    verify(exactly = 0) { userTokenRepository.updateRefreshToken(any(), any()) }
                    verify(exactly = 0) { jwtProvider.createAccessToken(any(), any(), any()) }
                }
            }

            context("JwtValidator 검증 단계에서 예외가 발생하면") {
                it("Service도 예외를 던진다") {
                    // given
                    every {
                        jwtValidator.validateAndExtractToken(oldRefreshTokenString)
                    } throws InvalidTokenException("Invalid JWT")

                    // when & then
                    shouldThrow<InvalidTokenException> {
                        jwtService.reissue(oldRefreshTokenString)
                    }
                }
            }
        }
    }
})