package finn.queryDto

import java.time.LocalDate
import java.util.*

data class KeywordsWithArticlesQueryDto(
    val keyword: String,
    val articles: List<UUID>,
    val sentiment: Int,
    val date: LocalDate
)
