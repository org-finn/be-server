package finn.entity

enum class PredictionStrategy(val strategy: String, val left: Int, val right: Int, val sentiment: Int) {
    STRONG_BUY("강한 매수", 80, 100, 1),
    WEEK_BUY("약한 매수", 60, 80, 1),
    NEUTRAL("관망", 40, 60, 0),
    WEEK_SELL("약한 매도", 20, 40, -1),
    STRONG_SELL("강한 매도", 0, 20, -1)
}