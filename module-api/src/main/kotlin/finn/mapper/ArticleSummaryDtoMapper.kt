package finn.mapper

import finn.entity.ArticleSummaryAll
import finn.response.articleSummary.ArticleSummaryAllResponse
import java.time.format.DateTimeFormatter

class ArticleSummaryDtoMapper {
    companion object {
        fun toDto(articleSummaryAll: ArticleSummaryAll): ArticleSummaryAllResponse {
            return ArticleSummaryAllResponse(
                articleSummaryAll.positiveReasoning,
                articleSummaryAll.negativeReasoning,
                articleSummaryAll.positiveKeywords,
                articleSummaryAll.negativeKeywords,
                articleSummaryAll.summaryDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            )
        }
    }
}