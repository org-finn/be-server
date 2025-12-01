package finn.mapper

import finn.entity.ArticleSummaryAll
import finn.entity.ArticleSummaryAllExposed
import java.time.ZoneId
import java.time.ZonedDateTime

fun toDomain(articleSummaryAll: ArticleSummaryAllExposed): ArticleSummaryAll {
    return ArticleSummaryAll.create(
        articleSummaryAll.id.value,
        ZonedDateTime.ofInstant(articleSummaryAll.summaryDate, ZoneId.of("Asia/Seoul")),
        articleSummaryAll.positiveReasoning.toString().split(","),
        articleSummaryAll.negativeReasoning.toString().split(","),
        articleSummaryAll.positiveKeywords.toString().split(","),
        articleSummaryAll.negativeKeywords.toString().split(",")
    )
}