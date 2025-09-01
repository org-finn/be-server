package finn.orchestrator

import finn.entity.command.ArticleC
import finn.entity.command.ArticleInsight
import finn.request.lambda.ArticleRealTimeBatchRequest
import finn.request.lambda.ArticleRealTimeBatchRequest.ArticleRealTimeRequest
import finn.request.lambda.ArticleRealTimeBatchRequest.ArticleRealTimeRequest.ArticleRealTimeInsightRequest
import finn.request.lambda.LambdaArticleRealTimeRequest
import finn.request.lambda.LambdaPredictionRequest
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
    fun saveArticle(request: LambdaArticleRealTimeRequest) {
        if (request.articles.isEmpty()) {
            return // 아티클 데이터가 없으므로, 더 이상 처리할 것이 없으므로 종료
        }
        log.debug { "요청된 tickerCode: ${request.tickerCode}" }

        // tickers가 비어있는 경우는 예측 로직 수행 x
        if (ArticleC.isNotProcessingPredictionArticles(request.article.tickers)) {
            val article = createArticle(request.article)
            val insights = createArticleInsights(request.article.insights)
            articleService.saveArticleList(article, insights)
            log.debug { "지원하는 티커가 없는 뉴스이므로 뉴스 저장만 수행합니다." }
            return
        }

        // Article에 필요한 Ticker 정보 조회
        val ticker = tickerService.getTickerByTickerCode(request.tickerCode)
        val tickerId = ticker.id
        val shortCompanyName = ticker.shortCompanyName
        val tickerCode = ticker.tickerCode

        // 각 Article 생성 및 저장
        val articleList = createArticleList(request, shortCompanyName, tickerId, tickerCode)

        articleService.saveArticleList(articleList)
    }
        // Article 생성 및 저장(Ticker_Article도 같이 생성)
        val article = createArticle(request.article)
        val insights = createArticleInsights(request.article.insights)
        articleService.saveArticleList(article, insights)

    @ExposedTransactional
    fun savePrediction(request: LambdaPredictionRequest) {
        val tickerId = request.tickerId
        val tickerCode = request.tickerCode
        val shortCompanyName = request.shortCompanyName
        val predictionDate = request.predictionDate

        log.debug { "${tickerCode}: 예측을 수행하여 저장합니다." }
        predictionService.savePrediction(
            tickerId,
            tickerCode,
            shortCompanyName,
            predictionDate,
            request.positiveArticleCount,
            request.negativeArticleCount,
            request.neutralArticleCount
        )
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