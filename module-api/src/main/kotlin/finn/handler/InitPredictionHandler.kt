package finn.handler

import finn.entity.query.PredictionStrategy
import finn.exception.NotSupportedTypeException
import finn.policy.isPreviousDayHoliday
import finn.queryDto.PredictionCreateDto
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
import java.math.BigDecimal
import java.sql.Connection
import java.util.*

@Component
class InitPredictionHandler(
    private val tickerQueryService: TickerQueryService,
    private val tickerCommandService: TickerCommandService,
    private val predictionCommandService: PredictionCommandService,
    private val predictionQueryService: PredictionQueryService,
    private val strategyFactory: StrategyFactory
) : PredictionHandler {

    override fun supports(type: String): Boolean = type == "init"

    override suspend fun handle(tasks: List<PredictionTask>) {
        val initTasks = tasks.filterIsInstance<InitPredictionTask>()
        if (initTasks.isEmpty()) return

        newSuspendedTransaction(
            context = Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        ) {
            val tickerIds = initTasks.map { it.tickerId }.distinct()

            val yesterdayAtrMap = tickerQueryService.findYesterdayAtrMap(tickerIds)
            val yesterdayVolatilityMap =
                predictionQueryService.findYesterdayVolatilityMap(tickerIds)

            val newPredictions = mutableListOf<PredictionCreateDto>()
            val tickerAtrUpdates = mutableMapOf<UUID, BigDecimal>()

            initTasks.forEach { task ->
                val tickerId = task.tickerId

                // 전략 검증
                val sentimentStrategy = strategyFactory.findSentimentScoreStrategy(task.type)
                        as? PredictionInitSentimentScoreStrategy
                    ?: throw NotSupportedTypeException("Invalid sentiment strategy")

                task.payload.recentScores =
                    predictionQueryService.getRecentSentimentScores(tickerId)

                val score = sentimentStrategy.calculate(task)

                var volatility: BigDecimal
                var todayAtr: BigDecimal?

                if (isPreviousDayHoliday()) {
                    // 휴일: 이전 Volatility 유지, ATR 업데이트 없음
                    volatility = yesterdayVolatilityMap[tickerId] ?: BigDecimal.ZERO
                } else {
                    // 평일: ATR 및 Volatility 계산
                    val technicalStrategy = strategyFactory.findTechnicalExponentStrategy(task.type)
                            as? ATRExponentStrategy
                        ?: throw NotSupportedTypeException("Invalid technical strategy")

                    // Bulk 조회한 Map에서 ATR 가져오기
                    task.payload.yesterdayAtr = yesterdayAtrMap[tickerId] ?: BigDecimal.ZERO

                    val calculated = technicalStrategy.calculate(task)
                    volatility = calculated.first.toBigDecimal()
                    todayAtr = calculated.second.toBigDecimal()

                    // Ticker 업데이트 대기열에 추가
                    tickerAtrUpdates[tickerId] = todayAtr
                }

                newPredictions.add(
                    PredictionCreateDto(
                        tickerId = tickerId,
                        tickerCode = task.payload.tickerCode,
                        shortCompanyName = task.payload.shortCompanyName,
                        score = score,
                        volatility = volatility,
                        predictionDate = task.payload.predictionDate.toLocalDateTime(),
                        positiveCount = 0,
                        negativeCount = 0,
                        neutralCount = 0,
                        sentiment = 0,
                        strategy = PredictionStrategy.NEUTRAL
                    )
                )
            }

            // 3. Bulk Insert & Update (쿼리 2번으로 축소)
            if (newPredictions.isNotEmpty()) {
                predictionCommandService.createPredictions(newPredictions)
            }

            if (tickerAtrUpdates.isNotEmpty()) {
                tickerCommandService.updateAtrs(tickerAtrUpdates)
            }
        }
    }
}