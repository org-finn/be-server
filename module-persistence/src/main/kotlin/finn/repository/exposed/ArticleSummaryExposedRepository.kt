package finn.repository.exposed

import finn.entity.ArticleSummaryAllExposed
import finn.entity.ArticleSummaryExposed
import finn.exception.CriticalDataOmittedException
import finn.exception.NotFoundDataException
import finn.table.ArticleSummaryAllTable
import finn.table.ArticleSummaryTable
import org.jetbrains.exposed.sql.SortOrder
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ArticleSummaryExposedRepository{

    fun findSummaryAll(): ArticleSummaryAllExposed {
        return ArticleSummaryAllExposed.all()
            .orderBy(ArticleSummaryAllTable.summaryDate to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?: throw CriticalDataOmittedException("종합 뉴스 요약 데이터를 찾을 수 없습니다.")
    }

    fun findByTickerId(tickerId: UUID): ArticleSummaryExposed {
        return ArticleSummaryExposed
            .find {
                (ArticleSummaryTable.tickerId eq tickerId)
            }
            .orderBy(ArticleSummaryTable.summaryDate to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?: throw NotFoundDataException("$tickerId 뉴스 요약 데이터를 찾을 수 없습니다.")

    }
}