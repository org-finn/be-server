package finn.orchestrator

import finn.entity.command.ArticleC
import finn.entity.command.ArticleInsight
import finn.request.lambda.LambdaArticleRealTimeRequest
import finn.request.lambda.LambdaArticleRealTimeRequest.LambdaArticle
import finn.request.lambda.LambdaArticleRealTimeRequest.LambdaArticle.ArticleRealTimeInsightRequest
import finn.request.lambda.LambdaPredictionRequest
import finn.service.ArticleCommandService
import finn.service.PredictionCommandService
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class LambdaOrchestrator(
    private val articleService: ArticleCommandService,
    private val predictionService: PredictionCommandService,
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @ExposedTransactional
    fun saveArticle(request: LambdaArticleRealTimeRequest) {
        // Article 생성 및 저장(Ticker_Article도 같이 생성)
        val article = createArticle(request.article)
        val insights = createArticleInsights(request.article.insights)
        articleService.saveArticleList(article, insights)
    }

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

    fun createArticle(article: LambdaArticle): ArticleC {
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