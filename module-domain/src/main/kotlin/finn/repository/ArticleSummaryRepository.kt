package finn.repository

import finn.entity.ArticleSummary
import finn.entity.ArticleSummaryAll
import java.util.*

interface ArticleSummaryRepository {

    fun findSummaryAll(): ArticleSummaryAll

    fun findSummaryByTickerId(tickerId: UUID): ArticleSummary
}