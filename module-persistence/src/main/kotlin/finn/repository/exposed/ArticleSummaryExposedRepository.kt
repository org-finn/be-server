package finn.repository.exposed

import finn.entity.ArticleSummaryAllExposed
import finn.exception.CriticalDataOmittedException
import finn.table.ArticleSummaryAllTable
import org.jetbrains.exposed.sql.javatime.date
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.LocalDate

@Repository
class ArticleSummaryExposedRepository(
    private val clock: Clock
) {

    fun findSummaryAll(): ArticleSummaryAllExposed {
        return ArticleSummaryAllExposed
            .find {
                ArticleSummaryAllTable.summaryDate.date() greaterEq LocalDate.now(clock)
            }
            .limit(1)
            .singleOrNull()
            ?: throw CriticalDataOmittedException("금일 날짜로 생성된 종합 뉴스 데이터를 찾을 수 없습니다.")
    }
}