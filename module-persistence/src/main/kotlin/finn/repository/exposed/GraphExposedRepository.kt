package finn.repository.exposed

import finn.exception.CriticalDataPollutedException
import finn.queryDto.TickerGraphQueryDto
import finn.table.TickerPriceTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Repository
class GraphExposedRepository {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    private data class TickerGraphQueryDtoImpl(
        val date: LocalDate,
        val price: BigDecimal,
        val changeRate: BigDecimal
    ) : TickerGraphQueryDto {
        override fun date(): LocalDate = date
        override fun price(): BigDecimal = price
        override fun changeRate(): BigDecimal = changeRate
    }

    fun findDaily(
        tickerId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TickerGraphQueryDto> {

        val maxAndCountResult = TickerPriceTable
            .select(TickerPriceTable.priceDate.max().date(), TickerPriceTable.id.count())
            .where {
                TickerPriceTable.tickerId eq tickerId and
                        TickerPriceTable.priceDate.date().between(
                            startDate,
                            endDate
                        )
            }
            .firstOrNull()

        val count = maxAndCountResult?.get(TickerPriceTable.id.count()) ?: 0L
        val maxDate = maxAndCountResult?.get(TickerPriceTable.priceDate.max().date())
        log.debug {"maxDate: $maxDate, from: $startDate, to: $endDate" }

        // 데이터가 전혀 없는 경우 예외 처리
        if (count == 0L || maxDate == null) {
            throw CriticalDataPollutedException("해당 id로 조회한 데이터가 없습니다, tickerId를 다시 확인해주세요.")
        }

        return TickerPriceTable
            .select(
                TickerPriceTable.priceDate,
                TickerPriceTable.close,
                TickerPriceTable.changeRate
            )
            .where {
                TickerPriceTable.tickerId eq tickerId and
                        TickerPriceTable.priceDate.date().between(
                            startDate,
                            endDate
                        )
            }
            .orderBy(TickerPriceTable.priceDate, SortOrder.ASC)
            .map { row ->
                TickerGraphQueryDtoImpl(
                    date = row[TickerPriceTable.priceDate].toLocalDate(),
                    price = row[TickerPriceTable.close],
                    changeRate = row[TickerPriceTable.changeRate]
                )
            }
    }

    /**
     * interval에 의해 등락률 동적 계산
     */
    fun findByInterval(
        tickerId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        interval: Int
    ): List<TickerGraphQueryDto> {
        // 1. 필요한 모든 가격 데이터를 DB에서 한 번에 조회하여 Map으로 변환
        val priceMap = TickerPriceTable
            .selectAll().where {
                (TickerPriceTable.tickerId eq tickerId) and
                        // 기준일 계산을 위해 startDate보다 interval만큼 더 이전 데이터까지 조회
                        TickerPriceTable.priceDate.date().between(
                            startDate.minusDays(interval.toLong()),
                            endDate
                        )
            }
            .orderBy(TickerPriceTable.priceDate, SortOrder.DESC) // 최신순으로 정렬
            .associate {
                it[TickerPriceTable.priceDate].toLocalDate() to it[TickerPriceTable.close]
            }

        if (priceMap.isEmpty()) {
            throw CriticalDataPollutedException("해당 id로 조회한 데이터가 없습니다, tickerId를 다시 확인해주세요.")
        }

        // 2. endDate부터 시작하여 interval만큼 차감하며 기준 날짜 목록 생성 및 등락률 계산
        val graphData = mutableListOf<TickerGraphQueryDto>()
        var currentDate = endDate

        while (currentDate > startDate) {
            // 3. 현재 날짜와 interval 이전 날짜에 대한 가장 가까운 영업일을 찾음
            val currentBusinessDay = findClosestBusinessDay(currentDate, priceMap.keys)
            val prevTargetDate = currentDate.minusDays(interval.toLong())
            val prevBusinessDay = findClosestBusinessDay(prevTargetDate, priceMap.keys)

            // 두 영업일이 모두 유효한 경우에만 계산 진행
            if (currentBusinessDay != null && prevBusinessDay != null) {
                val currentPrice = priceMap[currentBusinessDay]
                val prevPrice = priceMap[prevBusinessDay]

                // 4. 두 가격이 모두 존재하면 등락률을 계산하여 DTO 생성
                if (currentPrice != null && prevPrice != null && prevPrice != BigDecimal.ZERO) {
                    val changeRate =
                        ((currentPrice / prevPrice) - BigDecimal.ONE).multiply(BigDecimal(100))
                    graphData.add(
                        TickerGraphQueryDtoImpl(
                            date = currentBusinessDay,
                            price = currentPrice,
                            changeRate = changeRate
                        )
                    )
                }
            }
            // 다음 기준 날짜로 이동
            currentDate = prevTargetDate
        }

        // 날짜 오름차순으로 정렬하여 반환
        return graphData.sortedBy { it.date() }
    }


    /**
     * 가격 데이터가 존재하는 날짜들(businessDays) 중에서,
     * 주어진 날짜(date)와 같거나 그보다 과거인 가장 최근 날짜를 찾아 반환
     */
    private fun findClosestBusinessDay(date: LocalDate, businessDays: Set<LocalDate>): LocalDate? {
        return businessDays.filter { !it.isAfter(date) }.maxOrNull()
    }

}