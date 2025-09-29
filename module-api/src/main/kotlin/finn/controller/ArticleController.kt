package finn.controller

import finn.apiSpec.ArticleApiSpec
import finn.orchestrator.ArticleOrchestrator
import finn.paging.ArticlePageRequest
import finn.response.SuccessResponse
import finn.response.article.ArticleListResponse
import finn.response.article.ArticleTickerFilteringListResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class ArticleController(
    private val articleOrchestrator: ArticleOrchestrator
) : ArticleApiSpec {

    override fun getArticleList(
        pageRequest: ArticlePageRequest
    ): SuccessResponse<ArticleListResponse> {
        val response = articleOrchestrator.getRecentArticleList(pageRequest)
        return SuccessResponse("200 OK", "뉴스 목록을 성공적으로 조회하였습니다.", response)
    }

    override fun getFilteringTickerList(): SuccessResponse<ArticleTickerFilteringListResponse> {
        val response = articleOrchestrator.getTickerList()
        return SuccessResponse("200 OK", "티커 목록을 성공적으로 조회하였습니다.", response)
    }
}