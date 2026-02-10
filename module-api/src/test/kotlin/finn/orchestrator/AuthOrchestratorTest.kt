package finn.orchestrator


import finn.auth.JwtValidator
import finn.entity.UserInfo
import finn.entity.UserRole
import finn.entity.UserStatus
import finn.response.auth.GoogleIdTokenPayload
import finn.response.auth.GoogleIdTokenResponse
import finn.response.auth.TokenResponse
import finn.service.AuthService
import finn.service.JwtService
import finn.service.UserInfoService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import java.util.*

class AuthOrchestratorTest : DescribeSpec({

    // 1. 의존성 Mocking
    val authService = mockk<AuthService>()
    val userInfoService = mockk<UserInfoService>()
    val jwtService = mockk<JwtService>()
    val jwtValidator = mockk<JwtValidator>()

    // 2. 테스트 대상 주입
    val authOrchestrator = AuthOrchestrator(
        authService,
        userInfoService,
        jwtService,
        jwtValidator
    )

    describe("AuthOrchestrator") {

        // ==========================================
        // 1. getOAuthUserInfoForGoogle 테스트
        // ==========================================
        context("getOAuthUserInfoForGoogle 메서드는") {
            val authCode = "mock_auth_code"
            val idTokenString = "mock_id_token_string"
            val providerId = "1234567890"
            val email = "test@example.com"
            val imageUrl = "https://test.url"

            // Mock 응답 객체 준비
            val mockTokenResponse = mockk<GoogleIdTokenResponse>()
            val mockPayload = mockk<GoogleIdTokenPayload>()

            beforeEach {
                every { mockTokenResponse.idToken } returns idTokenString
                every { mockPayload.sub } returns providerId
                every { mockPayload.email } returns email
                every { mockPayload.picture } returns imageUrl
            }

            it("인증 코드를 받아 ID 토큰을 발급받고, 디코딩하여 유저 정보를 반환한다") {
                // given
                every { authService.issueIdTokenForGoogle(authCode) } returns mockTokenResponse
                every { authService.decodeIdToken(idTokenString) } returns mockPayload

                // when
                val result = authOrchestrator.getOAuthUserInfoForGoogle(authCode)

                // then
                result.provider shouldBe "google"
                result.providerId shouldBe providerId
                result.email shouldBe email
                result.imageUrl shouldBe imageUrl

                verify(exactly = 1) { authService.issueIdTokenForGoogle(authCode) }
                verify(exactly = 1) { authService.decodeIdToken(idTokenString) }
            }
        }

        // ==========================================
        // 2. accessOAuthUser 테스트 (핵심 분기)
        // ==========================================
        describe("accessOAuthUser 메서드는") {
            val provider = "google"
            val providerId = "1001"
            val email = "user@test.com"
            val imageUrl = "https://test.url"

            // Mocking을 위한 가짜 UserInfo 객체 (반환 타입에 맞게 가정)
            // 실제 엔티티나 DTO 구조에 맞춰 수정 필요
            val userId = UUID.randomUUID()

            context("이미 가입된 유저라면 (UserInfo 존재)") {
                // 가짜 기존 유저 객체
                val existingUserInfo =
                    mockk<UserInfo>(relaxed = true) // relaxed: 호출되지 않은 프로퍼티는 기본값 리턴

                beforeEach {
                    every { existingUserInfo.id } returns userId
                    every { existingUserInfo.imageUrl } returns imageUrl
                    every { existingUserInfo.role } returns UserRole.USER
                    every { existingUserInfo.status } returns UserStatus.REGISTERED

                    // given: 이미 존재함
                    every { authService.checkExistByOAuthUser(providerId) } returns existingUserInfo
                }

                it("새로 생성하지 않고 기존 유저 정보를 반환한다 (로그인)") {
                    // when
                    val result =
                        authOrchestrator.accessOAuthUser(provider, providerId, email, imageUrl)

                    // then
                    result.userId shouldBe userId
                    result.role shouldBe "USER"
                    result.status shouldBe "REGISTERED"
                    result.isNewUser shouldBe false

                    // Verify: 생성 로직이 호출되지 않았음을 검증
                    verify(exactly = 0) { authService.createOAuthUser(any(), any(), any()) }
                    verify(exactly = 0) { userInfoService.createUserInfo(any(), any()) }
                }
            }

            context("가입되지 않은 유저라면 (UserInfo 없음)") {
                val newOAuthUserId = UUID.randomUUID()
                val newUserInfo = mockk<UserInfo>(relaxed = true)
                val newImageUrl = "https://test.url"

                beforeEach {
                    every { newUserInfo.id } returns userId
                    every { newUserInfo.imageUrl } returns newImageUrl
                    every { newUserInfo.role } returns UserRole.USER
                    every { newUserInfo.status } returns UserStatus.REGISTERED

                    // given: 존재하지 않음 (null 반환)
                    every { authService.checkExistByOAuthUser(providerId) } returns null
                    // given: 생성 로직 Mock
                    every {
                        authService.createOAuthUser(
                            provider,
                            providerId,
                            email
                        )
                    } returns newOAuthUserId
                    every {
                        userInfoService.createUserInfo(
                            newOAuthUserId,
                            newImageUrl
                        )
                    } returns newUserInfo
                }

                it("OAuthUser와 UserInfo를 새로 생성하고 정보를 반환한다 (회원가입)") {
                    // when
                    val result =
                        authOrchestrator.accessOAuthUser(provider, providerId, email, imageUrl)

                    // then
                    result.userId shouldBe userId
                    result.isNewUser shouldBe true

                    // Verify: 순서대로 생성 로직 호출 확인
                    verifyOrder {
                        authService.checkExistByOAuthUser(providerId)
                        authService.createOAuthUser(provider, providerId, email)
                        userInfoService.createUserInfo(newOAuthUserId, newImageUrl)
                    }
                }
            }
        }

        // ==========================================
        // 3. issueToken 테스트
        // ==========================================
        context("issueToken 메서드는") {
            val userId = UUID.randomUUID()
            val role = "USER"
            val status = "REGISTERED"
            val deviceType = "APP"

            val mockTokenResponse =
                TokenResponse("access_token", "refresh_token")

            it("JwtService를 호출하여 토큰을 발급하고 반환한다") {
                // given
                every {
                    jwtService.issue(
                        userId,
                        role,
                        status,
                        deviceType
                    )
                } returns mockTokenResponse

                // when
                val result = authOrchestrator.issueToken(userId, role, status, deviceType)

                // then
                result shouldBe mockTokenResponse

                verify(exactly = 1) { jwtService.issue(userId, role, status, deviceType) }
            }
        }
    }
})