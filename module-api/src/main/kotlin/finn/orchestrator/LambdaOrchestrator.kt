package finn.orchestrator

import finn.entity.command.ArticleC
import finn.request.lambda.ArticleRealTimeBatchRequest
import finn.service.ArticleCommandService
import finn.service.PredictionCommandService
import finn.service.TickerQueryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service
import java.time.ZoneId

@Service
@ExposedTransactional(readOnly = true)
class LambdaOrchestrator(
    private val articleService: ArticleCommandService,
    private val predictionService: PredictionCommandService,
    private val tickerService: TickerQueryService
) {
    fun saveArticleAndPrediction(request: ArticleRealTimeBatchRequest) {
        if (request.articles.isEmpty()) {
            return // 아티클 데이터가 없으므로, 더 이상 처리할 것이 없으므로 종료
        }
        // Article에 필요한 Ticker 정보 조회
        val ticker = tickerService.getTickerByTickerCode(request.tickerCode)
        val tickerId = ticker.id
        val shortCompanyName = ticker.shortCompanyName
        val tickerCode = ticker.tickerCode

        // 각 Article 생성 및 저장
        val articleList = request.articles.asSequence()
            .map {
                ArticleC.create(
                    it.title,
                    it.description,
                    it.thumbnailUrl,
                    it.articleUrl,
                    it.publishedDate.atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                        .toLocalDateTime(), // 한국 시간 기준으로 저장해야함(n시간 전 등 구현 위함)
                    shortCompanyName,
                    it.author,
                    it.distinctId,
                    it.sentiment,
                    it.reasoning,
                    tickerId,
                    tickerCode
                )
            }.toList()

        articleService.saveArticleList(articleList)

        // isMarketOpen: True이면, Article Data들을 취합하여 Prediction 생성 및 저장
        if (request.isMarketOpen) { // 정규장 중에 수집된 뉴스 데이터이므로, 다음 주가 예측을 해야함
            predictionService.savePrediction(articleList, tickerId, tickerCode, shortCompanyName)
        }
    }

}