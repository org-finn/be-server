package finn.orchestrator

import finn.entity.command.ArticleC
import finn.entity.command.ArticleInsight
import finn.mapper.ArticleDtoMapper.Companion.toDto
import finn.mapper.TickerDtoMapper.Companion.toDto
import finn.paging.ArticlePageRequest
import finn.request.lambda.LambdaArticleRealTimeRequest.LambdaArticle
import finn.request.lambda.LambdaArticleRealTimeRequest.LambdaArticle.ArticleRealTimeInsightRequest
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

    fun getRecentArticleList(pageRequest: ArticlePageRequest): ArticleListResponse {
        val articleList = articleQueryService.getArticleDataList(pageRequest)
        return toDto(articleList)
    }

    fun getTickerList(): ArticleTickerFilteringListResponse {
        val tickerList = tickerQueryService.getAllTickerList()
        return toDto(tickerList)
    }

    fun getArticle(articleId: UUID): ArticleDetailResponse {
        val article = articleQueryService.getArticle(articleId)
        return toDto(article)
    }

    private fun createArticle(article: LambdaArticle): ArticleC {
        return ArticleC.create(
            article.title,
            article.description,
            article.thumbnailUrl,
            article.articleUrl,
            article.publishedDate.toLocalDateTime(),
            article.author,
            article.distinctId,
            article.tickers,
        )
    }

    private fun createArticleInsights(insights: List<ArticleRealTimeInsightRequest>): List<ArticleInsight> {
        return insights.map {
            ArticleInsight(
                it.tickerCode,
                it.sentiment,
                it.reasoning
            )
        }.toList()
    }
}