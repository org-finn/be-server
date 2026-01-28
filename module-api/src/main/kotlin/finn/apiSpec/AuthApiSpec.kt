package finn.apiSpec

import finn.request.auth.LogoutRequest
import finn.request.auth.OAuthLoginRequest
import finn.request.auth.ReIssueRequest
import finn.response.SuccessResponse
import finn.response.auth.ClientTokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "auth API", description = "auth 관련 API")
@RequestMapping("/api/v1")
interface AuthApiSpec {

    @Operation(
        summary = "로그인(by Google)", description = "인가코드를 기반으로 소셜 로그인 완료 후 토큰을 발급합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "구글 로그인 수행 후 액세스/리프레쉬 토큰 발급 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 인가코드 등의 이유로 인증에 실패하였습니다."
            ),
        ]
    )
    @PostMapping("/login/google")
    fun loginForGoogleOAuth(
        @RequestBody oAuthLoginRequest: OAuthLoginRequest
    ): ResponseEntity<SuccessResponse<ClientTokenResponse>>


    @Operation(
        summary = "토큰 재발급", description = "리프레쉬 토큰을 제출하여 액세스/리프레쉬 토큰을 재발급받습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "액세스/리프레쉬 토큰 재발급 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 리프레쉬 토큰으로 재발급에 실패하였습니다."
            ),
            ApiResponse(
                responseCode = "401",
                description = "리프레쉬 토큰이 누락되었습니다."
            ),
        ]
    )
    @PostMapping("/reIssue")
    fun reIssue(
        @RequestBody reIssueRequest: ReIssueRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<SuccessResponse<ClientTokenResponse>>


    @Operation(
        summary = "로그아웃", description = "로그아웃을 수행하고 쿠키 내의 토큰을 삭제합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그아웃 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 리프레쉬 토큰으로 로그아웃에 실패하였습니다."
            ),
            ApiResponse(
                responseCode = "401",
                description = "리프레쉬 토큰이 누락되었습니다."
            ),
        ]
    )
    @PostMapping("/logout")
    fun logout(
        @RequestBody logoutRequest: LogoutRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<SuccessResponse<Nothing>>

}