package finn.orchestrator

import finn.mapper.SearchDtoMapper
import finn.response.search.ArticleSearchListResponse
import finn.response.search.TickerSearchPreviewListResponse
import finn.service.ArticleQueryService
import finn.service.TickerQueryService
import finn.transaction.ExposedTransactional
import finn.validator.checkKeywordValid
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class SearchOrchestrator(
    private val tickerQueryService: TickerQueryService,
    private val articleQueryService: ArticleQueryService
) {

    fun getTickerSearchPreviewList(keyword: String?): TickerSearchPreviewListResponse {
        checkKeywordValid(keyword)
        val tickerDto = tickerQueryService.getTickerSearchList(keyword!!)
        return SearchDtoMapper.toDto(tickerDto)
    }

    fun getSearchTickerList(keyword: String?): TickerSearchPreviewListResponse {
        checkKeywordValid(keyword)
        val tickerDto = tickerQueryService.getTickerSearchList(keyword!!, limit = 3)
        return SearchDtoMapper.toDto(tickerDto)
    }

    fun getSearchArticleList(keyword: String?): ArticleSearchListResponse {
        checkKeywordValid(keyword)
        val articleDto = articleQueryService.searchArticles(keyword!!, limit = 3)
        return SearchDtoMapper.toArticleSearchDto(articleDto)
    }
}
