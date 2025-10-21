package finn.controller

import finn.apiSpec.ArticleApiSpec
import finn.orchestrator.ArticleOrchestrator
import finn.paging.ArticlePageRequest
import finn.response.SuccessResponse
import finn.response.article.ArticleDetailResponse
import finn.response.article.ArticleListResponse
import finn.response.article.ArticleTickerFilteringListResponse
import org.springframework.web.bind.annotation.RestController
import java.util.*

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

    override fun getArticle(articleId: UUID): SuccessResponse<ArticleDetailResponse> {
        val response = articleOrchestrator.getArticle(articleId)
        return SuccessResponse("200 OK", "아티클 상세 정보를 성공적으로 조회하였습니다.", response)
    }
}