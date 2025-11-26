package finn.queryDto

data class ArticleDetailTickerQueryDto(
    val shortCompanyName: String,
    val sentiment: String?,
    val reasoning: String?
)