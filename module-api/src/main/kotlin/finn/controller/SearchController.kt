package finn.controller

import finn.apiSpec.SearchApiSpec
import finn.orchestrator.SearchOrchestrator
import finn.response.SuccessResponse
import finn.response.search.TickerSearchPreviewListResponse
import finn.response.search.ArticleSearchListResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchController(
    private val searchOrchestrator: SearchOrchestrator
) : SearchApiSpec {

    override fun searchStocks(keyword: String?): SuccessResponse<TickerSearchPreviewListResponse> {
        val response = searchOrchestrator.getTickerSearchPreviewList(keyword)
        return SuccessResponse("200 OK", "종목 검색 결과를 성공적으로 조회하였습니다.", response)
    }

    override fun searchTickerList(keyword: String?): SuccessResponse<TickerSearchPreviewListResponse> {
        val response = searchOrchestrator.getSearchTickerList(keyword)
        return SuccessResponse("200 OK", "종목 검색 결과를 성공적으로 조회하였습니다.", response)
    }

    override fun searchArticleList(keyword: String?): SuccessResponse<ArticleSearchListResponse> {
        val response = searchOrchestrator.getSearchArticleList(keyword)
        return SuccessResponse("200 OK", "아티클 검색 결과 조회 성공", response)
    }
}
