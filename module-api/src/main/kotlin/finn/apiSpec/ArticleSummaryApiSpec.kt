package finn.apiSpec

import finn.response.SuccessResponse
import finn.response.articleSummary.ArticleSummaryAllResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "뉴스 요약 API", description = "뉴스 요약 데이터 관련 API")
@RequestMapping("/api/v1/article-summary")
interface ArticleSummaryApiSpec {
    @Operation(
        summary = "종합 뉴스 요약 조회", description = "오늘 일자의 종합 뉴스 요약 데이터를 조회합니다."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "뉴스 목록을 성공적으로 조회하였습니다."
        )]
    )
    @GetMapping("/all")
    fun getArticleSummaryForAll(): SuccessResponse<ArticleSummaryAllResponse>
}