package finn.moduleDomain.entity

import finn.moduleDomain.calculator.SentimentScoreCalculator
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
            neutralNewsCount: Int, sentimentScore: Int,
            predictionDate: LocalDateTime, collectedDate: LocalDateTime,
            todayScores: List<Int>, calculator: SentimentScoreCalculator
        ): Prediction {
            return Prediction(
                tickerId,
                tickerCode,
                shortCompanyName,
                positiveNewsCount,
                negativeNewsCount,
                neutralNewsCount,
                getSentimentScore(
                    tickerCode, collectedDate, todayScores,
                    positiveNewsCount, neutralNewsCount, negativeNewsCount, calculator
                ),
                getStrategyFromScore(sentimentScore),
                predictionDate
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
            } ?: throw IllegalArgumentException("유효하지 않은 sentimentScore: $sentimentScore")
        }
    }

    fun getNewsCountAlongWithStrategy(): Int {
        return when (predictionStrategy) {
            PredictionStrategy.STRONG_BUY, PredictionStrategy.WEEK_BUY -> positiveNewsCount
            PredictionStrategy.NEUTRAL -> neutralNewsCount
            PredictionStrategy.STRONG_SELL, PredictionStrategy.WEEK_SELL -> negativeNewsCount
        }
    }

}