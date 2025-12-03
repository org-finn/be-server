package finn.apiSpec

import finn.response.ErrorResponse
import finn.response.SuccessResponse
import finn.response.exchangeRate.ExchangeRateRealTimeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "환율 API", description = "환율 관련 API")
@RequestMapping("/api/v1/exchange-rate")
interface ExchangeRateApiSpec {
    @Operation(summary = "실시간 환율 조회", description = "실시간 환율 데이터를 조회합니다,")
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "실시간 환율 조회에 성공하였습니다."),
            ApiResponse(
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 조회 조건입니다.",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @GetMapping("/real-time")
    fun getExchangeRateRealTime(
        @Parameter(
            description = "인덱스 코드",
            required = true,
            example = "C01"
        ) @RequestParam indexCode: String
    ): SuccessResponse<ExchangeRateRealTimeResponse>
}