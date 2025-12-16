package finn.mapper

import finn.entity.ArticleSummary
import finn.entity.ArticleSummaryAll
import finn.entity.ArticleSummaryAllExposed
import finn.entity.ArticleSummaryExposed
import java.time.ZoneId
import java.time.ZonedDateTime

fun toDomain(articleSummaryAll: ArticleSummaryAllExposed): ArticleSummaryAll {
    return ArticleSummaryAll.create(
        articleSummaryAll.id.value,
        ZonedDateTime.ofInstant(articleSummaryAll.summaryDate, ZoneId.of("Asia/Seoul")),
        articleSummaryAll.positiveReasoning?.split("\n"),
        articleSummaryAll.negativeReasoning?.split("\n"),
        articleSummaryAll.positiveKeywords?.split(Regex("\\s*,\\s*")),
        articleSummaryAll.negativeKeywords?.split(Regex("\\s*,\\s*"))
    )
}

fun toDomain(articleSummary: ArticleSummaryExposed): ArticleSummary {
    return ArticleSummary.create(
        articleSummary.id.value,
        articleSummary.tickerId,
        articleSummary.summaryDate.atZone(ZoneId.of("Asia/Seoul")),
        articleSummary.positiveReasoning?.split("\n"),
        articleSummary.negativeReasoning?.split("\n"),
        articleSummary.positiveKeywords?.split(Regex("\\s*,\\s*")),
        articleSummary.negativeKeywords?.split(Regex("\\s*,\\s*"))
    )
}