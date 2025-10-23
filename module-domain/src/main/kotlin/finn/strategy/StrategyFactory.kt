package finn.strategy

import finn.exception.DomainPolicyViolationException
import org.springframework.stereotype.Component

@Component
class StrategyFactory(
    private val sentimentScoreStrategies: List<SentimentScoreStrategy<*>>,
    private val technicalExponentStrategies: List<TechnicalExponentStrategy<*>>,
) {
    fun findSentimentScoreStrategy(type: String): SentimentScoreStrategy<*> {
        return sentimentScoreStrategies.find { it.supports(type) }
            ?: throw DomainPolicyViolationException("지원하지 않는 계산 타입입니다: $type")
    }

    fun findTechnicalExponentStrategy(type: String): TechnicalExponentStrategy<*> {
        return technicalExponentStrategies.find { it.supports(type) }
            ?: throw DomainPolicyViolationException("지원하지 않는 계산 타입입니다: $type")
    }
}
