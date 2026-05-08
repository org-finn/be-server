package finn.apiSpec

import finn.response.ErrorResponse
import finn.response.SuccessResponse
import finn.response.search.ArticleSearchListResponse
import finn.response.search.SearchPreviewResponse
import finn.response.search.TickerSearchListResponse
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

@Tag(name = "검색 API", description = "검색(자동 완성) 관련 API")
@RequestMapping("/api/v1")
interface SearchApiSpec {

    @Operation(summary = "통합 검색(자동 완성) 미리보기", description = "키워드를 기반으로 종목 및 게시글을 검색하여 미리보기 결과를 제공합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "통합 검색 미리보기 결과를 성공적으로 조회하였습니다."),
            ApiResponse(
                responseCode = "400",
                description = "키워드는 2글자 이상만 요청할 수 있습니다.",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @GetMapping("/search-preview")
    fun searchPreview(
        @Parameter(
            description = "검색 키워드 (2글자 이상)",
            required = true,
            example = "Ap"
        ) @RequestParam keyword: String?
    ): SuccessResponse<SearchPreviewResponse>
    
    @Operation(summary = "통합 검색 - 종목 검색 결과", description = "키워드를 기반으로 종목 검색 결과를 최대 3개까지 제공합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "종목 검색 결과를 성공적으로 조회하였습니다."),
            ApiResponse(
                responseCode = "400",
                description = "키워드는 2글자 이상만 요청할 수 있습니다.",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @GetMapping("/search/ticker")
    fun searchTickerList(
        @Parameter(
            description = "검색 키워드 (2글자 이상)",
            required = true,
            example = "Ap"
        ) @RequestParam keyword: String?
    ): SuccessResponse<TickerSearchListResponse>
    
    @Operation(summary = "통합 검색 - 아티클 검색 결과", description = "키워드를 기반으로 아티클 검색 결과를 최대 3개까지 제공합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "아티클 검색 결과 조회 성공"),
            ApiResponse(
                responseCode = "400",
                description = "키워드는 2글자 이상만 요청할 수 있습니다.",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @GetMapping("/search/article")
    fun searchArticleList(
        @Parameter(
            description = "검색 키워드 (2글자 이상)",
            required = true,
            example = "Ap"
        ) @RequestParam keyword: String?
    ): SuccessResponse<ArticleSearchListResponse>
}
