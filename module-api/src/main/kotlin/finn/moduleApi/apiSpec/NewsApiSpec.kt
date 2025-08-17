package finn.moduleApi.apiSpec

import finn.moduleApi.paging.NewsPageRequest
import finn.moduleApi.response.ErrorResponse
import finn.moduleApi.response.SuccessResponse
import finn.moduleApi.response.news.NewsListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "뉴스 조회 API", description = "뉴스 데이터 조회 관련 API")
@RequestMapping("/api/v1/news")
interface NewsApiSpec {
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
    fun getNewsList(
        @Parameter(
            description = "정렬 기준",
            schema = Schema(type = "string", defaultValue = "recent")
        ) @RequestParam(defaultValue = "recent") sort: String,
        @Parameter(
            description = "필터링 옵션",
            schema = Schema(
                type = "string",
                defaultValue = "all",
                allowableValues = ["all", "positive", "negative"]
            )
        ) @RequestParam(defaultValue = "all") filter: String,

        @ParameterObject pageRequest: NewsPageRequest
    ): SuccessResponse<NewsListResponse>
}