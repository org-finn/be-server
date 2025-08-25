package finn.repository.query

import finn.exception.CriticalDataOmittedException
import finn.queryDto.TickerGraphQueryDto
import finn.table.NIntervalChangeRateTable
import finn.table.TickerPriceTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.mod
import org.jetbrains.exposed.sql.javatime.dateLiteral
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Repository
class GraphQueryRepository {

    private data class TickerGraphQueryDtoImpl(
        val date: LocalDate,
        val price: BigDecimal,
        val changeRate: BigDecimal
    ) : TickerGraphQueryDto {
        override fun date(): LocalDate = date
        override fun price(): BigDecimal = price
        override fun changeRate(): BigDecimal = changeRate
    }

    fun findByInterval(
        tickerId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        interval: Int,
        minimumCount: Long
    ): List<TickerGraphQueryDto> {

        val maxAndCountResult = TickerPriceTable
            .select(TickerPriceTable.priceDate.max(), TickerPriceTable.id.count())
            .where {
                TickerPriceTable.tickerId eq tickerId
                TickerPriceTable.priceDate greater startDate
                TickerPriceTable.priceDate less endDate
            }
            .firstOrNull()

        val count = maxAndCountResult?.get(TickerPriceTable.id.count()) ?: 0L
        val maxDate = maxAndCountResult?.get(TickerPriceTable.priceDate.max())

        // 데이터가 전혀 없는 경우 예외 처리
        if (count == 0L || maxDate == null) {
            throw CriticalDataOmittedException("치명적 오류: 조회 범위 내에 데이터가 존재하지 않습니다.")
        }

        // A. 데이터 개수가 minimum_count 보다 적으면, interval 단위를 무시하고 전체 데이터 반환
        if (count < minimumCount) {
            return TickerPriceTable
                .join(
                    NIntervalChangeRateTable,
                    JoinType.INNER,
                    additionalConstraint = {
                        (NIntervalChangeRateTable.tickerId eq TickerPriceTable.tickerId) and
                                (NIntervalChangeRateTable.priceDate eq TickerPriceTable.priceDate) and
                                (NIntervalChangeRateTable.interval eq interval)
                    }
                )
                .select(
                    TickerPriceTable.priceDate,
                    TickerPriceTable.close,
                    NIntervalChangeRateTable.changeRate
                )
                .where {
                    TickerPriceTable.tickerId eq tickerId
                    TickerPriceTable.priceDate greater startDate
                    TickerPriceTable.priceDate less endDate
                }
                .orderBy(TickerPriceTable.priceDate, SortOrder.ASC)
                .map { row ->
                    TickerGraphQueryDtoImpl(
                        date = row[TickerPriceTable.priceDate],
                        price = row[TickerPriceTable.close],
                        changeRate = row[NIntervalChangeRateTable.changeRate]
                    )
                }
        }
        // B. 데이터 개수가 충분하면, interval 적용하여 데이터 반환
        else {
            val startDateLiteral = dateLiteral(startDate)
            val intervalCondition =
                (DaysBetween(TickerPriceTable.priceDate, startDateLiteral) mod interval) eq 0

            return TickerPriceTable
                .join(
                    NIntervalChangeRateTable,
                    JoinType.INNER,
                    additionalConstraint = {
                        (NIntervalChangeRateTable.tickerId eq TickerPriceTable.tickerId) and
                                (NIntervalChangeRateTable.priceDate eq TickerPriceTable.priceDate) and
                                (NIntervalChangeRateTable.interval eq interval)
                    }
                )
                .select(
                    TickerPriceTable.priceDate,
                    TickerPriceTable.close,
                    NIntervalChangeRateTable.changeRate
                )
                .where {
                    TickerPriceTable.tickerId eq tickerId
                    TickerPriceTable.priceDate greater startDate
                    TickerPriceTable.priceDate less endDate
                    // interval 조건을 만족하거나, 가장 최신 날짜의 데이터는 항상 포함
                    (intervalCondition or (TickerPriceTable.priceDate eq maxDate))
                }
                .orderBy(TickerPriceTable.priceDate, SortOrder.ASC)
                .map { row ->
                    TickerGraphQueryDtoImpl(
                        date = row[TickerPriceTable.priceDate],
                        price = row[TickerPriceTable.close],
                        changeRate = row[NIntervalChangeRateTable.changeRate]
                    )
                }
        }
    }

    inner class DaysBetween(
        private val date1: Expression<*>,
        private val date2: Expression<*>
    ) : Function<Int>(IntegerColumnType()) {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) {
            queryBuilder.append("CAST(DATE_PART('day', AGE(", date1, ", ", date2, ")) AS INTEGER)")
        }
    }
}