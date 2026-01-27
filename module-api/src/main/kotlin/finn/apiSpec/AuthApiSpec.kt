package finn.apiSpec

import finn.request.auth.OAuthLoginRequest
import finn.response.SuccessResponse
import finn.response.auth.ClientTokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "auth API", description = "auth 관련 API")
@RequestMapping("/api/v1")
interface AuthApiSpec {

    @Operation(
        summary = "구글 토큰 발급", description = "인가코드를 기반으로 구글 ID 토큰을 발급합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그인을 수행하여 토큰 발급에 성공하였습니다."
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 인가코드입니다."
            ),
            ApiResponse(
                responseCode = "404",
                description = "발급 업체로부터 회원 리소스를 조회할 수 없습니다."
            ),
        ]
    )
    @PostMapping("/login/google")
    fun loginForGoogleOAuth(
        @RequestBody oAuthLoginRequest: OAuthLoginRequest
    ): ResponseEntity<SuccessResponse<ClientTokenResponse>>

}