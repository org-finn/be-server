package finn.service

import finn.auth.JwtProvider
import finn.auth.JwtValidator
import finn.entity.RefreshToken
import finn.entity.UserInfo
import finn.entity.UserToken
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
    // save, update, delete 등 void 메서드가 많으므로 relaxed = true 사용
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
            // Provider가 반환하는 객체 Mocking
            val mockRefreshTokenObj = mockk<RefreshToken>()
            val mockRtValue = "mock_refresh_token_string"
            val mockIssuedAt = Date()
            val mockExpiredAt = Date(System.currentTimeMillis() + 100000)

            beforeEach {
                // RefreshToken 객체 프로퍼티 설정
                every { mockRefreshTokenObj.tokenValue } returns mockRtValue
                every { mockRefreshTokenObj.issuedAt } returns mockIssuedAt
                every { mockRefreshTokenObj.expiredAt } returns mockExpiredAt
            }

            it("Access 토큰과 Refresh 토큰을 생성하고, DB에 저장 로직을 호출해야 한다") {
                // given
                every { jwtProvider.createAccessToken(userId, role, status) } returns mockAccessToken
                every { jwtProvider.createRefreshToken(any()) } returns mockRefreshTokenObj

                // when
                val response = jwtService.issue(userId, role, status, deviceType)

                // then
                response.accessToken shouldBe mockAccessToken
                response.refreshToken shouldBe mockRtValue

                // Verify: DB 저장 메서드가 올바른 파라미터로 호출되었는지 검증
                verify(exactly = 1) {
                    userTokenRepository.save(
                        userId = userId,
                        deviceId = any(), // 내부에서 randomUUID 생성하므로 any()로 매칭
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
            val dbRefreshTokenValue = "user_submitted_refresh_token" // 일치하는 경우
            val deviceId = UUID.randomUUID()
            val userId = UUID.randomUUID()

            // Mock 객체들 준비
            val mockExtractedToken = mockk<RefreshToken>() // Validator가 추출한 토큰 객체
            val mockDbTokenEntity = mockk<UserToken>() // DB에서 조회된 엔티티
            val mockUserInfo = mockk<UserInfo>()

            // 새로 발급될 토큰들
            val mockNewAccessToken = "new_access_token"
            val mockNewRefreshTokenObj = mockk<RefreshToken>()
            val mockNewRefreshTokenValue = "new_refresh_token_value"

            beforeEach {
                clearMocks(jwtProvider, jwtValidator, userTokenRepository, userInfoRepository)

                // 1. Validator Mock: 문자열 -> 객체 추출
                every { mockExtractedToken.deviceId } returns deviceId
                every { jwtValidator.validateAndExtractRefreshToken(userRefreshTokenString) } returns mockExtractedToken

                // 2. Repo Mock: DeviceId -> DB 엔티티 조회
                every { mockDbTokenEntity.refreshToken } returns dbRefreshTokenValue
                every { mockDbTokenEntity.userId } returns userId
                every { userTokenRepository.findByDeviceId(deviceId) } returns mockDbTokenEntity

                // 3. Validator Mock: 문자열 비교
                every { jwtValidator.refreshTokenEquals(any(), any()) } returns true

                // 4. UserInfo Repo Mock
                every { mockUserInfo.id } returns userId
                every { mockUserInfo.role.name } returns "MEMBER"
                every { mockUserInfo.status.name } returns "ACTIVE"
                every { userInfoRepository.findById(userId) } returns mockUserInfo

                // 5. Provider Mock: 새 토큰 생성
                every { mockNewRefreshTokenObj.tokenValue } returns mockNewRefreshTokenValue
                every { jwtProvider.createAccessToken(any(), any(), any()) } returns mockNewAccessToken
                every { jwtProvider.createRefreshToken(deviceId) } returns mockNewRefreshTokenObj
            }

            context("정상적인 Refresh Token이고 DB 값과 일치하면") {
                it("Rotation을 수행(DB Update)하고 새 토큰을 반환한다") {
                    // when
                    val response = jwtService.reissue(userRefreshTokenString)

                    // then
                    response.accessToken shouldBe mockNewAccessToken
                    response.refreshToken shouldBe mockNewRefreshTokenValue

                    // Verify: Rotation (Update) 호출 확인
                    verify(exactly = 1) {
                        userTokenRepository.updateRefreshToken(mockNewRefreshTokenValue, deviceId)
                    }
                    // Verify: Delete는 호출되지 않아야 함
                    verify(exactly = 0) {
                        userTokenRepository.deleteRefreshToken(any())
                    }
                }
            }

            context("DB에 저장된 토큰 값과 요청받은 토큰 값이 일치하지 않으면 (토큰 탈취 시도)") {
                it("해당 기기의 Refresh Token을 삭제하고 예외를 던진다") {
                    // given
                    // DB에는 다른 값이 저장되어 있다고 가정
                    val differentDbTokenValue = "different_db_token"
                    every { mockDbTokenEntity.refreshToken } returns differentDbTokenValue

                    // Validator가 false 반환
                    every { jwtValidator.refreshTokenEquals(userRefreshTokenString, differentDbTokenValue) } returns false

                    // when & then
                    shouldThrow<InvalidTokenException> {
                        jwtService.reissue(userRefreshTokenString)
                    }

                    // Verify: 탈취 감지 시 삭제 로직 실행 확인
                    verify(exactly = 1) {
                        userTokenRepository.deleteRefreshToken(deviceId)
                    }
                    // Verify: 업데이트나 생성 로직은 실행되지 않음
                    verify(exactly = 0) { userTokenRepository.updateRefreshToken(any(), any()) }
                    verify(exactly = 0) { jwtProvider.createAccessToken(any(), any(), any()) }
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
                        jwtService.reissue(userRefreshTokenString)
                    }
                }
            }
        }
    }
})