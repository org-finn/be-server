package finn.entity

import finn.calculator.SentimentScoreCalculator
import finn.exception.BadRequestDomainPolicyViolationException
import java.time.LocalDateTime
import java.util.*

class Prediction private constructor(
    val tickerId: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val positiveNewsCount: Long,
    val negativeNewsCount: Long,
    val neutralNewsCount: Long,
    val sentimentScore: Int,
    val sentiment: Int,
    val predictionStrategy: PredictionStrategy,
    val predictionDate: LocalDateTime
) {

    companion object {
        fun create(
            tickerId: UUID, tickerCode: String, shortCompanyName: String,
            positiveNewsCount: Long, negativeNewsCount: Long,
            neutralNewsCount: Long, predictionDate: LocalDateTime, collectedDate: LocalDateTime,
            todayScores: List<Int>, calculator: SentimentScoreCalculator
        ): Prediction {
            val calculatedScore = getSentimentScore(
                tickerCode, collectedDate, todayScores,
                positiveNewsCount, neutralNewsCount, negativeNewsCount, calculator
            )
            val strategy = getStrategyFromScore(calculatedScore)

            return Prediction(
                tickerId = tickerId,
                tickerCode = tickerCode,
                shortCompanyName = shortCompanyName,
                positiveNewsCount = positiveNewsCount,
                negativeNewsCount = negativeNewsCount,
                neutralNewsCount = neutralNewsCount,
                sentimentScore = calculatedScore,
                predictionStrategy = strategy,
                sentiment = getSentiment(strategy),
                predictionDate = predictionDate
            )
        }

        fun getSentimentScore(
            tickerCode: String,
            collectedDate: LocalDateTime,
            todayScores: List<Int>,
            positiveNewsCount: Long,
            neutralNewsCount: Long,
            negativeNewsCount: Long,
            calculator: SentimentScoreCalculator
        ): Int {
            return calculator.calculateScore(
                tickerCode, collectedDate, todayScores,
                positiveNewsCount, neutralNewsCount, negativeNewsCount
            )
        }

        fun getStrategyFromScore(sentimentScore: Int): PredictionStrategy {
            return PredictionStrategy.entries.firstOrNull { strategy ->
                sentimentScore > strategy.left && sentimentScore <= strategy.right
            }
                ?: throw BadRequestDomainPolicyViolationException("유효하지 않은 sentimentScore: $sentimentScore")
        }

        fun getSentiment(predictionStrategy: PredictionStrategy): Int {
            return when (predictionStrategy) {
                PredictionStrategy.STRONG_BUY, PredictionStrategy.WEEK_BUY -> 1
                PredictionStrategy.NEUTRAL -> 0
                PredictionStrategy.STRONG_SELL, PredictionStrategy.WEEK_SELL -> -1
            }
        }
    }

    fun getNewsCountAlongWithStrategy(): Long {
        return when (predictionStrategy) {
            PredictionStrategy.STRONG_BUY, PredictionStrategy.WEEK_BUY -> positiveNewsCount
            PredictionStrategy.NEUTRAL -> neutralNewsCount
            PredictionStrategy.STRONG_SELL, PredictionStrategy.WEEK_SELL -> negativeNewsCount
        }
    }
}