package finn.repository

import finn.entity.command.ArticleInsight
import java.time.LocalDateTime
import java.util.*

interface ArticleTickerRepository {
    fun saveArticleTicker(
        articleId: UUID,
        title: String,
        insights: List<ArticleInsight>,
        publishedDate: LocalDateTime
    )
}