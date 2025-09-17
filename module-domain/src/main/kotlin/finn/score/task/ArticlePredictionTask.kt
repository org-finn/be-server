package finn.score.task

import java.time.OffsetDateTime
import java.util.*
import kotlin.properties.Delegates

data class ArticlePredictionTask(
    override val tickerId: UUID,
    val payload: ArticlePayload
) : PredictionTask() {
    override val type: String = "article"

    data class ArticlePayload(
        val predictionDate: OffsetDateTime,
        val positiveArticleCount: Long,
        val negativeArticleCount: Long,
        val neutralArticleCount: Long,
        val createdAt: OffsetDateTime,
    ) {
        var previousScore: Int by Delegates.notNull()
    }
}
