package finn.mapper

import finn.entity.ArticleSummaryAll
import finn.entity.ArticleSummaryAllExposed
import java.time.ZoneId
import java.time.ZonedDateTime

fun toDomain(articleSummaryAll: ArticleSummaryAllExposed): ArticleSummaryAll {
    return ArticleSummaryAll.create(
        articleSummaryAll.id.value,
        ZonedDateTime.ofInstant(articleSummaryAll.summaryDate, ZoneId.of("Asia/Seoul")),
        articleSummaryAll.positiveReasoning?.split("\n"),
        articleSummaryAll.negativeReasoning?.split("\n"),
        articleSummaryAll.positiveKeywords?.trim()?.split(",")?.map { it.trim() },
        articleSummaryAll.negativeKeywords?.trim()?.split(",")?.map { it.trim() }
    )
}