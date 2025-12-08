package finn.response.articleSummary

data class ArticleSummaryAllResponse(
    val positiveReasoning: List<String>?,
    val negativeReasoning: List<String>?,
    val positiveKeywords: List<String>?,
    val negativeKeywords: List<String>?,
    val summaryDate: String
)
