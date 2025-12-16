package finn.mapper

import finn.entity.ArticleSummary
import finn.entity.ArticleSummaryAll
import finn.response.articleSummary.ArticleSummaryAllResponse
import finn.response.articleSummary.ArticleSummaryTickerResponse
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

        fun toDto(articleSummary: ArticleSummary): ArticleSummaryTickerResponse {
            return ArticleSummaryTickerResponse(
                articleSummary.tickerId.toString(),
                articleSummary.positiveReasoning,
                articleSummary.negativeReasoning,
                articleSummary.positiveKeywords,
                articleSummary.negativeKeywords,
                articleSummary.summaryDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            )
        }
    }
}