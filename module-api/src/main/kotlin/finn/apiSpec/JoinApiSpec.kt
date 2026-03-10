package finn.apiSpec

import finn.response.ErrorResponse
import finn.response.SuccessResponse
import finn.response.userinfo.JoinTickerResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "회원가입 API", description = "회원가입 관련 API")
@RequestMapping("/api/v1/join")
interface JoinApiSpec {

    @Operation(
        summary = "종목 리스트 조회", description = "종목 리스트를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "종목 리스트 조회 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 조회 조건입니다.",
                content = arrayOf(
                    Content(
                        schema = Schema(implementation = ErrorResponse::class)
                    )
                )
            )
        ]
    )
    @GetMapping("/tickers")
    fun getTickerList(
        @RequestParam page: Int,
        @RequestParam(required = false) keyword: String?
    ): SuccessResponse<JoinTickerResponse>


}