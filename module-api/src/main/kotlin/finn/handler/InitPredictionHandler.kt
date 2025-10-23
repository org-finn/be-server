package finn.handler

import finn.exception.NotSupportedTypeException
import finn.policy.isPreviousDayHoliday
import finn.service.PredictionCommandService
import finn.service.PredictionQueryService
import finn.service.TickerCommandService
import finn.service.TickerQueryService
import finn.strategy.ATRExponentStrategy
import finn.strategy.PredictionInitSentimentScoreStrategy
import finn.strategy.StrategyFactory
import finn.task.InitPredictionTask
import finn.task.PredictionTask
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Component
import java.sql.Connection

@Component
class InitPredictionHandler(
    private val tickerQueryService: TickerQueryService,
    private val tickerCommandService: TickerCommandService,
    private val predictionCommandService: PredictionCommandService,
    private val predictionQueryService: PredictionQueryService,
    private val strategyFactory: StrategyFactory
) : PredictionHandler {

    override fun supports(type: String): Boolean = type == "init"

    override suspend fun handle(task: PredictionTask) {
        newSuspendedTransaction(
            context = Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        ) {
            if (task !is InitPredictionTask) {
                throw NotSupportedTypeException("Unsupported prediction task type in Init Prediction: ${task.type}")
            }
            val tickerId = task.tickerId
            val tickerCode = task.payload.tickerCode
            val shortCompanyName = task.payload.shortCompanyName
            val predictionDate = task.payload.predictionDate

            val sentimentScoreStrategy = strategyFactory.findSentimentScoreStrategy(task.type)
            if (sentimentScoreStrategy !is PredictionInitSentimentScoreStrategy) {
                throw NotSupportedTypeException("Unsupported sentiment score strategy in Init Prediction: ${sentimentScoreStrategy.javaClass}")
            }
            task.payload.recentScores = predictionQueryService.getRecentSentimentScores(tickerId)
            val score = sentimentScoreStrategy.calculate(task)

            // 금일이 일/월인지 여부 검사: TickerPrice가 들어오지 않는 날은 계산하지 않고, 이전일 volatility 복사, todayAtr 업데이트 하지않음
            if (isPreviousDayHoliday()) {
                val volatility = predictionQueryService.getYesterdayVolatility(tickerId)
                predictionCommandService.createPrediction(
                    tickerId,
                    tickerCode,
                    shortCompanyName,
                    score,
                    volatility,
                    predictionDate
                )
            } else {
                val technicalExponentStrategy =
                    strategyFactory.findTechnicalExponentStrategy(task.type)
                if (technicalExponentStrategy !is ATRExponentStrategy) {
                    throw NotSupportedTypeException("Unsupported technical exponent strategy in Init Prediction: ${sentimentScoreStrategy.javaClass}")
                }
                task.payload.yesterdayAtr = tickerQueryService.getYesterdayAtr(tickerId)
                val volatilityAndAtr = technicalExponentStrategy.calculate(task)

                val volatility = volatilityAndAtr.first.toBigDecimal()
                val todayAtr = volatilityAndAtr.second.toBigDecimal()

                predictionCommandService.createPrediction(
                    tickerId,
                    tickerCode,
                    shortCompanyName,
                    score,
                    volatility,
                    predictionDate
                )
                tickerCommandService.updateAtr(tickerId, todayAtr)
            }
        }
    }
}