package finn.apiSpec

import finn.response.SuccessResponse
import finn.response.marketstatus.TodayMarketStatusResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "시장 정보 검색 API", description = "시장 정보 관련 API")
@RequestMapping("/api/v1/market-status")
interface MarketStatusApiSpec {
    @Operation(summary = "금일 주식 시장 정보 조회", description = "금일 주식 시장 정보를 제공합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "금일 주식 시장 정보를 성공적으로 조회하였습니다."),
            ApiResponse(
            )]
    )
    @GetMapping("/ticker/today")
    fun getTodayTickerMarketStatus(): SuccessResponse<TodayMarketStatusResponse>
}