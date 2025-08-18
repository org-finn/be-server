package finn.moduleDomain.entity

import finn.moduleDomain.calculator.SentimentScoreCalculator
import finn.moduleDomain.exception.BadRequestDomainPolicyViolationException
import java.time.LocalDateTime
import java.util.*

class Prediction private constructor(
    val tickerId: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val positiveNewsCount: Int,
    val negativeNewsCount: Int,
    val neutralNewsCount: Int,
    val sentimentScore: Int,
    val predictionStrategy: PredictionStrategy,
    val predictionDate: LocalDateTime
) {

    companion object {
        fun create(
            tickerId: UUID, tickerCode: String, shortCompanyName: String,
            positiveNewsCount: Int, negativeNewsCount: Int,
            neutralNewsCount: Int, predictionDate: LocalDateTime, collectedDate: LocalDateTime,
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
                predictionDate = predictionDate
            )
        }

        fun getSentimentScore(
            tickerCode: String,
            collectedDate: LocalDateTime,
            todayScores: List<Int>,
            positiveNewsCount: Int,
            neutralNewsCount: Int,
            negativeNewsCount: Int,
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
    }

    fun getNewsCountAlongWithStrategy(): Int {
        return when (predictionStrategy) {
            PredictionStrategy.STRONG_BUY, PredictionStrategy.WEEK_BUY -> positiveNewsCount
            PredictionStrategy.NEUTRAL -> neutralNewsCount
            PredictionStrategy.STRONG_SELL, PredictionStrategy.WEEK_SELL -> negativeNewsCount
        }
    }

    fun getSentiment(): Int {
        return when (predictionStrategy) {
            PredictionStrategy.STRONG_BUY, PredictionStrategy.WEEK_BUY -> 1
            PredictionStrategy.NEUTRAL -> 0
            PredictionStrategy.STRONG_SELL, PredictionStrategy.WEEK_SELL -> -1
        }
    }

}