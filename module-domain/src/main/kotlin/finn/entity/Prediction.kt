package finn.entity

import finn.calculator.SentimentScoreCalculator
import finn.exception.DomainPolicyViolationException
import java.time.LocalDateTime
import java.util.*

class Prediction private constructor(
    val tickerId: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val positiveArticleCount: Long,
    val negativeArticleCount: Long,
    val neutralArticleCount: Long,
    val sentimentScore: Int,
    val sentiment: Int,
    val predictionStrategy: PredictionStrategy,
    val predictionDate: LocalDateTime
) {

    companion object {
        fun create(
            tickerId: UUID, tickerCode: String, shortCompanyName: String,
            positiveArticleCount: Long, negativeArticleCount: Long,
            neutralArticleCount: Long, predictionDate: LocalDateTime, collectedDate: LocalDateTime,
            todayScores: List<Int>, calculator: SentimentScoreCalculator
        ): Prediction {
            val calculatedScore = getSentimentScore(
                tickerCode, collectedDate, todayScores,
                positiveArticleCount, neutralArticleCount, negativeArticleCount, calculator
            )
            val strategy = getStrategyFromScore(calculatedScore)

            return Prediction(
                tickerId = tickerId,
                tickerCode = tickerCode,
                shortCompanyName = shortCompanyName,
                positiveArticleCount = positiveArticleCount,
                negativeArticleCount = negativeArticleCount,
                neutralArticleCount = neutralArticleCount,
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
            positiveArticleCount: Long,
            neutralArticleCount: Long,
            negativeArticleCount: Long,
            calculator: SentimentScoreCalculator
        ): Int {
            return calculator.calculateScore(
                tickerCode, collectedDate, todayScores,
                positiveArticleCount, neutralArticleCount, negativeArticleCount
            )
        }

        fun getStrategyFromScore(sentimentScore: Int): PredictionStrategy {
            return PredictionStrategy.entries.firstOrNull { strategy ->
                sentimentScore > strategy.left && sentimentScore <= strategy.right
            }
                ?: throw DomainPolicyViolationException("유효하지 않은 sentimentScore: $sentimentScore")
        }

        fun getSentiment(predictionStrategy: PredictionStrategy): Int {
            return when (predictionStrategy) {
                PredictionStrategy.STRONG_BUY, PredictionStrategy.WEEK_BUY -> 1
                PredictionStrategy.NEUTRAL -> 0
                PredictionStrategy.STRONG_SELL, PredictionStrategy.WEEK_SELL -> -1
            }
        }
    }

    fun getArticleCountAlongWithStrategy(): Long {
        return when (predictionStrategy) {
            PredictionStrategy.STRONG_BUY, PredictionStrategy.WEEK_BUY -> positiveArticleCount
            PredictionStrategy.NEUTRAL -> neutralArticleCount
            PredictionStrategy.STRONG_SELL, PredictionStrategy.WEEK_SELL -> negativeArticleCount
        }
    }
}