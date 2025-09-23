package finn.strategy

import finn.exception.DomainPolicyViolationException
import org.springframework.stereotype.Component

@Component
class StrategyFactory(
    private val strategies: List<SentimentScoreStrategy<*>>
) {
    fun findStrategy(type: String): SentimentScoreStrategy<*> {
        return strategies.find { it.supports(type) }
            ?: throw DomainPolicyViolationException("지원하지 않는 계산 타입입니다: $type")
    }
}
