package finn.apiSpec

import finn.response.ErrorResponse
import finn.response.SuccessResponse
import finn.response.graph.TickerGraphResponse
import finn.response.graph.TickerRealTimeGraphListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "티커 가격 API", description = "티커 가격 관련 API")
@RequestMapping("/api/v1/price/ticker")
@ResponseStatus(HttpStatus.OK)
interface TickerPriceApiSpec {

    @Operation(
        summary = "실거래가 그래프 데이터 조회",
        description = "기간(period)에 따라 특정 종목의 실제 종가 그래프 데이터를 조회합니다."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "종목 그래프 데이터를 성공적으로 조회하였습니다."
        ), ApiResponse(
            responseCode = "400",
            description = "지원하지 않는 기간(period)입니다.",
            content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
        ), ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 종목 Id 값입니다.",
            content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
        )]
    )
    @GetMapping("/{tickerId}/graph")
    fun getGraphData(
        @Parameter(
            description = "종목 ID (UUID)",
            required = true,
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
        ) @PathVariable tickerId: UUID,
        @Parameter(
            description = "조회 기간",
            schema = Schema(
                type = "string",
                defaultValue = "now",
                allowableValues = ["2W", "1M", "6M", "1Y"]
            )
        ) @RequestParam(defaultValue = "now") period: String
    ): SuccessResponse<TickerGraphResponse>


    @Operation(
        summary = "실시간 주가 데이터 조회",
        description = "특정 종목의 실시간 종가 그래프 데이터를 조회합니다."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "종목 그래프 데이터를 성공적으로 조회하였습니다."
        ), ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 종목 Id 값입니다.",
            content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
        )]
    )
    @GetMapping("/{tickerId}/real-time")
    fun getRealTimeGraphData(
        @Parameter(
            description = "종목 ID (UUID)",
            required = true,
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
        ) @PathVariable tickerId: UUID,
    ): SuccessResponse<TickerRealTimeGraphListResponse>
}