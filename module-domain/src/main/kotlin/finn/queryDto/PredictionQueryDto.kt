package finn.queryDto

import java.time.LocalDateTime
import java.util.*

data class PredictionQueryDto(
    val predictionDate: LocalDateTime,
    val tickerId: UUID,
    val shortCompanyName: String,
    val tickerCode: String,
    val predictionStrategy: String,
    val sentiment: Int,
    val articleCount: Long,
    var positiveKeywords: String?,
    var negativeKeywords: String?,
    var isFavorite: Boolean?,
    var articleTitles: List<ArticleTitleQueryDto>?,
    var graphData: PredictionListGraphDataQueryDto?
)