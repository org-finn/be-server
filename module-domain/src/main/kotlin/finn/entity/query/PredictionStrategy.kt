package finn.entity.query

import finn.exception.DomainPolicyViolationException

enum class PredictionStrategy(
    val strategy: String,
    val left: Int,
    val right: Int,
    val sentiment: Int
) {
    STRONG_BUY("강한 호재", 80, 100, 1),
    WEEK_BUY("약한 호재", 60, 80, 1),
    NEUTRAL("관망", 40, 60, 0),
    WEEK_SELL("약한 악재", 20, 40, -1),
    STRONG_SELL("강한 악재", 0, 20, -1);

    companion object {
        fun findByStrategy(strategy: String): PredictionStrategy {
            return PredictionStrategy.entries.find { it.strategy == strategy }
                ?: throw DomainPolicyViolationException("${strategy}: 유효하지 않은 예측 전략입니다.")
        }
    }

}