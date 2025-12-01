package finn.mapper

import finn.entity.ArticleSummaryAll
import finn.response.articleSummary.ArticleSummaryAllResponse

class ArticleSummaryDtoMapper {
    companion object {
        fun toDto(articleSummaryAll: ArticleSummaryAll): ArticleSummaryAllResponse {
            return ArticleSummaryAllResponse(
                articleSummaryAll.positiveReasoning,
                articleSummaryAll.negativeReasoning,
                articleSummaryAll.positiveKeywords,
                articleSummaryAll.negativeKeywords,
                articleSummaryAll.summaryDate.toString()
            )
        }
    }
}