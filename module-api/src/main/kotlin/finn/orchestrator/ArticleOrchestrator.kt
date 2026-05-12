package finn.orchestrator

import finn.mapper.ArticleDtoMapper.Companion.toDto
import finn.mapper.TickerDtoMapper.Companion.toDto
import finn.paging.ArticlePageRequest
import finn.response.article.ArticleDetailResponse
import finn.response.article.ArticleListResponse
import finn.response.article.ArticleTickerFilteringListResponse
import finn.service.ArticleQueryService
import finn.service.TickerQueryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class ArticleOrchestrator(
    private val articleQueryService: ArticleQueryService,
    private val tickerQueryService: TickerQueryService
) {

    fun getRecentArticleList(userId: UUID?, pageRequest: ArticlePageRequest): ArticleListResponse {
        val articleList = articleQueryService.getArticleDataList(userId, pageRequest)
        return toDto(articleList)
    }

    fun getTickerList(): ArticleTickerFilteringListResponse {
        val tickerList = tickerQueryService.getAllTickerList()
        return toDto(tickerList)
    }

    fun getArticle(userId: UUID?, articleId: UUID): ArticleDetailResponse {
        val article = articleQueryService.getArticle(userId, articleId)
        return toDto(article)
    }
}