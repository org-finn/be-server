package finn.mapper

import finn.entity.ArticleSummaryAll
import finn.entity.ArticleSummaryAllExposed
import java.time.ZoneId
import java.time.ZonedDateTime

fun toDomain(articleSummaryAll: ArticleSummaryAllExposed): ArticleSummaryAll {
    return ArticleSummaryAll.create(
        articleSummaryAll.id.value,
        ZonedDateTime.ofInstant(articleSummaryAll.summaryDate, ZoneId.of("Asia/Seoul")),
        articleSummaryAll.positiveReasoning.toString().split("\n"),
        articleSummaryAll.negativeReasoning.toString().split("\n"),
        articleSummaryAll.positiveKeywords.toString().trim().split(","),
        articleSummaryAll.negativeKeywords.toString().trim().split(",")
    )
}