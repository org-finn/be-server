package finn.queryDto

import java.time.LocalDate

data class KeywordsWithArticlesQueryDto(
    val keyword: String,
    val articles: List<ArticleIdAndTitleQueryDto>,
    val sentiment: Int,
    val date: LocalDate
)
