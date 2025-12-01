package finn.repository

import finn.entity.ArticleSummaryAll

interface ArticleSummaryRepository {

    fun findSummaryAll(): ArticleSummaryAll
}