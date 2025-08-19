package finn.controller

import finn.apiSpec.NewsApiSpec
import finn.orchestrator.NewsOrchestrator
import finn.paging.NewsPageRequest
import finn.response.SuccessResponse
import finn.response.news.NewsListResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class NewsController(
    private val newsOrchestrator: NewsOrchestrator
) : NewsApiSpec {

    override fun getNewsList(
        pageRequest: NewsPageRequest
    ): SuccessResponse<NewsListResponse> {
        val response = newsOrchestrator.getRecentNewsList(pageRequest)
        return SuccessResponse("200 OK", "뉴스 목록을 성공적으로 조회하였습니다.", response)
    }
}