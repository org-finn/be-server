package finn.repository.exposed

import finn.entity.ArticleSummaryAllExposed
import finn.entity.ArticleSummaryExposed
import finn.exception.CriticalDataOmittedException
import finn.table.ArticleSummaryAllTable
import finn.table.ArticleSummaryTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.date
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.LocalDate
import java.util.*

@Repository
class ArticleSummaryExposedRepository(
    private val clock: Clock
) {

    fun findSummaryAll(): ArticleSummaryAllExposed {
        return ArticleSummaryAllExposed
            .find {
                ArticleSummaryAllTable.summaryDate.date() greaterEq LocalDate.now(clock)
            }
            .orderBy(ArticleSummaryAllTable.summaryDate to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?: throw CriticalDataOmittedException("금일 날짜로 생성된 종합 뉴스 데이터를 찾을 수 없습니다.")
    }

    fun findByTickerId(tickerId: UUID): ArticleSummaryExposed {
        return ArticleSummaryExposed
            .find {
                (ArticleSummaryTable.tickerId eq tickerId) and
                        (ArticleSummaryTable.summaryDate.date() greaterEq LocalDate.now(clock))
            }
            .orderBy(ArticleSummaryTable.summaryDate to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?: throw CriticalDataOmittedException("금일 날짜로 생성된 $tickerId 뉴스 데이터를 찾을 수 없습니다.")

    }
}