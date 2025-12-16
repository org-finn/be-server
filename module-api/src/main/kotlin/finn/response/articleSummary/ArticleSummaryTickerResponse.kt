package finn.response.articleSummary

data class ArticleSummaryTickerResponse(
    val tickerId: String,
    val positiveReasoning: List<String>?,
    val negativeReasoning: List<String>?,
    val positiveKeywords: List<String>?,
    val negativeKeywords: List<String>?,
    val summaryDate: String
)
