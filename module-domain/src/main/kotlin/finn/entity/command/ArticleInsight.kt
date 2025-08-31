package finn.entity.command

data class ArticleInsight(
    val tickerCode : String,
    val sentiment: String?,
    val reasoning: String?,
)
