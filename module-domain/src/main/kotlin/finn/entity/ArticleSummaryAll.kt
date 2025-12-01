package finn.entity

import java.time.ZonedDateTime
import java.util.*

class ArticleSummaryAll private constructor(
    val id: UUID,
    val summaryDate: ZonedDateTime,
    val positiveReasoning: List<String>?,
    val negativeReasoning: List<String>?,
    val positiveKeywords: List<String>?,
    val negativeKeywords: List<String>?,
) {
    companion object {
        fun create(
            id: UUID,
            summaryDate: ZonedDateTime,
            positiveReasoning: List<String>?,
            negativeReasoning: List<String>?,
            positiveKeywords: List<String>?,
            negativeKeywords: List<String>?,
        ): ArticleSummaryAll {
            return ArticleSummaryAll(
                id = id,
                summaryDate = summaryDate,
                positiveReasoning = positiveReasoning,
                negativeReasoning = negativeReasoning,
                positiveKeywords = positiveKeywords,
                negativeKeywords = negativeKeywords,
            )
        }
    }
}