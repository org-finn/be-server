package finn.repository.exposed

import finn.entity.ArticleSummaryAllExposed
import finn.entity.ArticleSummaryExposed
import finn.exception.CriticalDataOmittedException
import finn.exception.CriticalDataPollutedException
import finn.exception.NotFoundDataException
import finn.table.ArticleSummaryAllTable
import finn.table.ArticleSummaryTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.date
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
class ArticleSummaryExposedRepository {

    fun findSummaryAll(): ArticleSummaryAllExposed {
        return ArticleSummaryAllExposed.all()
            .orderBy(ArticleSummaryAllTable.summaryDate to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?: throw CriticalDataOmittedException("종합 뉴스 요약 데이터를 찾을 수 없습니다.")
    }

    fun findByTickerIdAndDate(tickerId: UUID, date: String): ArticleSummaryExposed {
        val date = runCatching {
            LocalDate.parse(date)
        }.getOrElse { throw CriticalDataPollutedException("날짜 형식이 올바르지 않습니다. 올바른 형식은 YYYY-MM-DD 입니다.") }

        return ArticleSummaryExposed
            .find {
                (ArticleSummaryTable.tickerId eq tickerId) and (ArticleSummaryTable.summaryDate.date() eq date)
            }
            .orderBy(ArticleSummaryTable.summaryDate to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?: throw NotFoundDataException("$date 날짜의 $tickerId 뉴스 요약 데이터를 찾을 수 없습니다.")

    }
}