package finn.controller

import finn.apiSpec.ArticleSummaryApiSpec
import finn.orchestrator.ArticleSummaryOrchestrator
import finn.response.SuccessResponse
import finn.response.articleSummary.ArticleSummaryAllResponse
import finn.response.articleSummary.ArticleSummaryTickerResponse
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ArticleSummaryController(
    private val articleSummaryOrchestrator: ArticleSummaryOrchestrator
) : ArticleSummaryApiSpec {

    override fun getArticleSummaryForAll(): SuccessResponse<ArticleSummaryAllResponse> {
        val response = articleSummaryOrchestrator.getArticleSummaryForAll()
        return SuccessResponse("200 OK", "종합 뉴스 요약 데이터 조회에 성공하였습니다.", response)
    }

    override fun getArticleSummaryForTicker(tickerId: UUID): SuccessResponse<ArticleSummaryTickerResponse> {
        val response = articleSummaryOrchestrator.getArticleSummaryForTicker(tickerId)
        return SuccessResponse("200 OK", "종목 뉴스 요약 데이터 조회에 성공하였습니다.", response)
    }
}