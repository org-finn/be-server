package finn.moduleDomain.entity

enum class PredictionStrategy(val strategy: String, val left: Int, val right: Int) {
    STRONG_BUY("강한 매수", 80, 100),
    WEEK_BUY("약한 매수", 60, 80),
    NEUTRAL("중립", 40, 60),
    WEEK_SELL("약한 매도", 20, 40),
    STRONG_SELL("강한 매도", 0, 20)
}