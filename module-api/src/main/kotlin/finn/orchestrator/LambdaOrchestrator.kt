package finn.orchestrator

import finn.entity.Article
import finn.request.lambda.ArticleRealTimeBatchRequest
import finn.service.ArticleCommandService
import finn.service.PredictionCommandService
import finn.service.TickerQueryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class LambdaOrchestrator(
    private val articleService: ArticleCommandService,
    private val predictionService: PredictionCommandService,
    private val tickerService: TickerQueryService
) {
    fun saveArticleAndPrediction(request: ArticleRealTimeBatchRequest) {
        // Article에 필요한 Ticker 정보 조회
        val ticker = tickerService.getTickerByTickerCode(request.tickerCode)
        val tickerId = ticker.id
        val shortCompanyName = ticker.shortCompanyName

        // 각 Article 생성 및 저장
        val articleList = request.articles.asSequence()
            .map { it ->
                Article.create(
                    UUID.randomUUID(),
                    it.title,
                    it.description,
                    it.thumbnailUrl,
                    it.articleUrl,
                    it.publishedDate.toLocalDateTime(),
                    shortCompanyName,
                    it.author,
                    it.sentiment,
                    it.reasoning,
                    tickerId
                )
            }.toList()

        articleService.saveArticleList(articleList)

        // isMarketOpen: True이면, Article Data들을 취합하여 Prediction 생성 및 저장
        if (request.isMarketOpen) { // 정규장 중에 수집된 뉴스 데이터이므로, 다음 주가 예측을 해야함
            predictionService.savePrediction(articleList)
        }
    }

}