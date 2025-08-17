package finn.moduleDomain.entity

enum class PredictionStrategy(val strategy: String) {
    STRONG_BUY("강한 매수"),
    WEEK_BUY("약한 매수"),
    NEUTRAL("중립"),
    WEEK_SELL("약한 매도"),
    STRONG_SELL("강한 매도")
}