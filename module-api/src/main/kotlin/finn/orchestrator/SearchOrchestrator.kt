package finn.orchestrator

import finn.mapper.SearchDtoMapper.Companion.toSearchListDto
import finn.mapper.SearchDtoMapper.Companion.toSearchPreviewDto
import finn.response.search.ArticleSearchListResponse
import finn.response.search.SearchPreviewResponse
import finn.response.search.TickerSearchListResponse
import finn.service.ArticleQueryService
import finn.service.PredictionQueryService
import finn.service.TickerQueryService
import finn.transaction.ExposedTransactional
import finn.validator.checkKeywordValid
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class SearchOrchestrator(
    private val tickerQueryService: TickerQueryService,
    private val predictionQueryService: PredictionQueryService,
    private val articleQueryService: ArticleQueryService
) {

    fun getSearchPreview(keyword: String?): SearchPreviewResponse {
        checkKeywordValid(keyword)
        val tickerSearchList = tickerQueryService.getTickerSearchList(keyword!!)
        val articleSearchList = articleQueryService.searchArticles(keyword)
        return toSearchPreviewDto(tickerSearchList, articleSearchList)
    }

    fun getSearchTickerList(keyword: String?): TickerSearchListResponse {
        checkKeywordValid(keyword)
        val predictionDto = predictionQueryService.searchTickers(keyword!!)
        return toSearchListDto(predictionDto)
    }

    fun getSearchArticleList(keyword: String?): ArticleSearchListResponse {
        checkKeywordValid(keyword)
        val articleDto = articleQueryService.searchArticles(keyword!!, limit = 3)
        return toSearchListDto(articleDto)
    }
}
