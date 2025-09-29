package finn.orchestrator

import finn.mapper.TickerDtoMapper
import finn.mapper.toDto
import finn.paging.ArticlePageRequest
import finn.response.article.ArticleListResponse
import finn.response.article.ArticleTickerFilteringListResponse
import finn.service.ArticleQueryService
import finn.service.TickerQueryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class ArticleOrchestrator(
    private val articleQueryService: ArticleQueryService,
    private val tickerQueryService: TickerQueryService
) {

    fun getRecentArticleList(pageRequest: ArticlePageRequest): ArticleListResponse {
        val articleList = articleQueryService.getArticleDataList(pageRequest)
        return toDto(articleList)
    }

    fun getTickerList(): ArticleTickerFilteringListResponse {
        val tickerList = tickerQueryService.getAllTickerList()
        return TickerDtoMapper.toDto(tickerList)
    }
}