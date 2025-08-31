package finn.orchestrator

import finn.entity.command.ArticleC
import finn.entity.command.ArticleInsight
import finn.request.lambda.ArticleRealTimeBatchRequest
import finn.request.lambda.ArticleRealTimeBatchRequest.ArticleRealTimeRequest
import finn.request.lambda.ArticleRealTimeBatchRequest.ArticleRealTimeRequest.ArticleRealTimeInsightRequest
import finn.service.ArticleCommandService
import finn.service.PredictionCommandService
import finn.service.TickerQueryService
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class LambdaOrchestrator(
    private val articleService: ArticleCommandService,
    private val predictionService: PredictionCommandService,
    private val tickerService: TickerQueryService
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @ExposedTransactional
    fun saveArticleAndPrediction(request: ArticleRealTimeBatchRequest) {
        log.debug { "요청된 article distinct_id: ${request.article.distinctId}" }

        // tickers가 비어있는 경우는 예측 로직 수행 x
        if (ArticleC.isNotProcessingPredictionArticles(request.article.tickers)) {
            val article = createArticle(request.article)
            val insights = createArticleInsights(request.article.insights)
            articleService.saveArticleList(article, insights)
            log.debug { "지원하는 티커가 없는 뉴스이므로 뉴스 저장만 수행합니다." }
            return
        }

        // Article 생성 및 저장(Ticker_Article도 같이 생성)
        val article = createArticle(request.article)
        val insights = createArticleInsights(request.article.insights)
        articleService.saveArticleList(article, insights)

        // isMarketOpen: True이면, Article Data들을 취합하여 Prediction 생성 및 저장
        if (request.isMarketOpen) { // 정규장 중에 수집된 뉴스 데이터이므로, 다음 주가 예측을 해야함
            log.debug { "정규장 시간이므로 예측을 수행하여 저장합니다." }
            val predictionDate = request.predictionDate
            predictionService.savePrediction(
                articleList,
                tickerId,
                tickerCode,
                shortCompanyName,
                predictionDate
            )
        }
    }

    fun createArticle(article: ArticleRealTimeRequest): ArticleC {
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

    fun createArticleInsights(insights: List<ArticleRealTimeInsightRequest>): List<ArticleInsight> {
        return insights.map {
            ArticleInsight(
                it.tickerCode,
                it.sentiment,
                it.reasoning
            )
        }.toList()
    }

}