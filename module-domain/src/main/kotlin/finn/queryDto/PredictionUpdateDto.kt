package finn.queryDto

import java.time.LocalDateTime
import java.util.*

data class PredictionUpdateDto(
    val tickerId: UUID,
    val positiveArticleCount: Long,
    val negativeArticleCount: Long,
    val neutralArticleCount: Long,
    val score: Int,
    val sentiment: Int,
    val strategy: String,
    val predictionDate: LocalDateTime
) {
}