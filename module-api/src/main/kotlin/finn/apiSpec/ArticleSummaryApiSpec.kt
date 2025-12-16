package finn.apiSpec

import finn.response.ErrorResponse
import finn.response.SuccessResponse
import finn.response.articleSummary.ArticleSummaryAllResponse
import finn.response.articleSummary.ArticleSummaryTickerResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@Tag(name = "뉴스 요약 API", description = "뉴스 요약 데이터 관련 API")
@RequestMapping("/api/v1/article-summary")
interface ArticleSummaryApiSpec {
    @Operation(
        summary = "종합 뉴스 요약 조회", description = "오늘 일자의 종합 뉴스 요약 데이터를 조회합니다."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "전체 뉴스 요약을 성공적으로 조회하였습니다."
        )]
    )
    @GetMapping("/all")
    fun getArticleSummaryForAll(): SuccessResponse<ArticleSummaryAllResponse>

    @Operation(
        summary = "종목 뉴스 요약 조회", description = "오늘 일자의 개별 종목 뉴스 요약 데이터를 조회합니다."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "종목 뉴스 요약을 성공적으로 조회하였습니다."
        ), ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 종목 Id 값입니다.",
            content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
        )]
    )
    @GetMapping("/{tickerId}")
    fun getArticleSummaryForTicker(
        @Parameter(
            description = "종목 ID (UUID)",
            required = true,
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
        )
        @PathVariable tickerId: UUID
    ): SuccessResponse<ArticleSummaryTickerResponse>
}