package finn.apiSpec

import finn.paging.ArticlePageRequest
import finn.response.ErrorResponse
import finn.response.SuccessResponse
import finn.response.article.ArticleDetailResponse
import finn.response.article.ArticleListResponse
import finn.response.article.ArticleTickerFilteringListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@Tag(name = "뉴스 조회 API", description = "뉴스 데이터 조회 관련 API")
@RequestMapping("/api/v1/article")
interface ArticleApiSpec {
    @Operation(
        summary = "뉴스 리스트 조회", description = "필터와 정렬 옵션을 적용하여 뉴스 리스트를 조회합니다."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "뉴스 목록을 성공적으로 조회하였습니다."
        ), ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 조회 조건입니다.",
            content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
        )]
    )
    @GetMapping
    fun getArticleList(
        @ParameterObject pageRequest: ArticlePageRequest
    ): SuccessResponse<ArticleListResponse>

    @Operation(
        summary = "뉴스 필터링 티커 목록 조회", description = "필터링 조건에 사용되는 티커 리스트를 조회합니다."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "티커 목록을 성공적으로 조회하였습니다."
        )]
    )
    @GetMapping("/ticker-list")
    fun getFilteringTickerList(): SuccessResponse<ArticleTickerFilteringListResponse>


    @Operation(
        summary = "아티클 디테일 조회", description = "아티클 상세 정보를 조회합니다."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "아티클을 성공적으로 조회하였습니다."
        ), ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 조회 조건입니다.",
            content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
        )]
    )
    @GetMapping("/{articleId}")
    fun getArticle(
        @Parameter(
            description = "아티클 ID (UUID)",
            required = true,
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
        ) @PathVariable articleId: UUID
    ): SuccessResponse<ArticleDetailResponse>
}