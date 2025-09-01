package finn.orchestrator

import finn.entity.command.ArticleC
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

        // 특정 종목 관련이 아닌 뉴스는 예측과 관련없으므로 예측 로직 수행 x
        if (ArticleC.isNotProcessingPredictionArticles(request.tickerCode)) {
            val articleList = createArticleListGeneral(request)
            articleService.saveArticleList(articleList)
            log.debug { "GENERAL에 해당하는 뉴스들이므로 뉴스 저장만 수행합니다." }
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
            request.positiveNewsCount,
            request.negativeNewsCount,
            request.neutralNewsCount
        )
    }

    private fun createArticleListGeneral(request: LambdaArticleRealTimeRequest): List<ArticleC> =
        request.articles.asSequence()
            .map {
                ArticleC.create(
                    it.title,
                    it.description,
                    it.thumbnailUrl,
                    it.articleUrl,
                    it.publishedDate.toLocalDateTime(),
                    null,
                    it.author,
                    it.distinctId,
                    it.sentiment,
                    it.reasoning,
                    null,
                    null
                )
            }.toList()

    private fun createArticleList(
        request: LambdaArticleRealTimeRequest,
        shortCompanyName: String,
        tickerId: UUID,
        tickerCode: String
    ): List<ArticleC> = request.articles.asSequence()
        .map {
            ArticleC.create(
                it.title,
                it.description,
                it.thumbnailUrl,
                it.articleUrl,
                it.publishedDate.toLocalDateTime(),
                shortCompanyName,
                it.author,
                it.distinctId,
                it.sentiment,
                it.reasoning,
                tickerId,
                tickerCode
            )
        }.toList()
}