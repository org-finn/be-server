package finn.moduleApi.apiSpec

import finn.moduleApi.response.ErrorResponse
import finn.moduleApi.response.SuccessResponse
import finn.moduleApi.response.search.TickerSearchPreviewListResponse
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

@Tag(name = "종목 검색 API", description = "종목 검색(자동 완성) 관련 API")
@RequestMapping("/api/v1")
interface SearchApiSpec {
    @Operation(summary = "종목 검색(자동 완성)", description = "키워드를 기반으로 종목을 검색하여 자동 완성 목록을 제공합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "종목 검색 결과를 성공적으로 조회하였습니다."), ApiResponse(
            responseCode = "400",
            description = "키워드는 2글자 이상만 요청할 수 있습니다.",
            content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
        )]
    )
    @GetMapping("/search-preview/ticker")
    fun searchStocks(
        @Parameter(
            description = "검색 키워드 (2글자 이상)",
            required = true,
            example = "Ap"
        ) @RequestParam keyword: String?
    ): SuccessResponse<TickerSearchPreviewListResponse>
}