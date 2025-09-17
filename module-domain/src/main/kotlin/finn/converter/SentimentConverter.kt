package finn.converter

import finn.entity.query.PredictionStrategy
import finn.exception.DomainPolicyViolationException
import org.springframework.stereotype.Component

@Component
class SentimentConverter {

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