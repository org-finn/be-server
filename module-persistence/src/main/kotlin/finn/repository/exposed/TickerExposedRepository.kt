package finn.repository.exposed

import finn.entity.TickerExposed
import finn.exception.DomainPolicyViolationException
import finn.exception.NotFoundDataException
import finn.queryDto.TickerQueryDto
import finn.table.TickerPriceTable
import finn.table.TickerTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

@Repository
class TickerExposedRepository {

    fun findTickerListBySearchKeyword(keyword: String): List<TickerQueryDto> {
        return TickerTable.select(
            TickerTable.id,
            TickerTable.code,
            TickerTable.shortCompanyName,
            TickerTable.fullCompanyName
        ).where { TickerTable.shortCompanyName.lowerCase() like "${keyword.lowercase()}%" }
            .map { row ->
                TickerQueryDto(
                    tickerId = row[TickerTable.id].value,
                    tickerCode = row[TickerTable.code],
                    shortCompanyName = row[TickerTable.shortCompanyName],
                    shortCompanyNameKr = row[TickerTable.shortCompanyNameKr],
                    fullCompanyName = row[TickerTable.fullCompanyName]
                )
            }
    }

    fun findByTickerCode(tickerCode: String): TickerExposed {
        return TickerExposed.find { TickerTable.code eq tickerCode }
            .single()
    }

    fun findTickerMapByTickerCodeList(tickerCodeList: List<String>): Map<String, UUID> {
        return TickerTable.select(
            TickerTable.id, TickerTable.code
        ).where { TickerTable.code inList tickerCodeList }
            .associate { it[TickerTable.code] to it[TickerTable.id].value }
    }

    fun findAll(): List<TickerQueryDto> {
        return TickerTable.selectAll()
            .orderBy(TickerTable.shortCompanyName, SortOrder.ASC)
            .map { row ->
                TickerQueryDto(
                    tickerId = row[TickerTable.id].value,
                    tickerCode = row[TickerTable.code],
                    shortCompanyName = row[TickerTable.shortCompanyName],
                    shortCompanyNameKr = row[TickerTable.shortCompanyNameKr],
                    fullCompanyName = row[TickerTable.fullCompanyName]
                )
            }
    }

    suspend fun findPreviousAtrByTickerId(tickerId: UUID): BigDecimal {
        return TickerPriceTable.select(TickerPriceTable.atr)
            .where {
                (TickerPriceTable.tickerId eq tickerId)
            }
            .orderBy(TickerPriceTable.priceDate, SortOrder.DESC)
            .limit(1, offset = 1) // 1개만 가져오되, 1개를 건너뜀 (즉, 2번째 행)
            .map {
                it[TickerPriceTable.atr]
            }.firstOrNull()
            ?: throw NotFoundDataException("최근 ${tickerId}의 ATR이 존재하지 않습니다.")
    }

    fun updateTodayAtrByTickerId(tickerId: UUID, todayAtr: BigDecimal) {
        // 가장 최신 데이터의 'id'를 찾는 서브쿼리를 정의
        val subQuery = TickerPriceTable
            .select(TickerPriceTable.id)
            .where { TickerPriceTable.tickerId eq tickerId }
            .orderBy(TickerPriceTable.priceDate, SortOrder.DESC)
            .limit(1)

        val updatedRowCount = TickerPriceTable.update({ TickerPriceTable.id eqSubQuery subQuery }) {
            it[TickerPriceTable.atr] = todayAtr
        }

        if (updatedRowCount == 0) {
            throw NotFoundDataException("업데이트할 ${tickerId}의 최신 가격 데이터가 존재하지 않습니다.")
        }
    }

    fun findAtrsByIds(ids: List<UUID>): Map<UUID, BigDecimal> {
        if (ids.isEmpty()) return emptyMap()

        return TickerPriceTable.select(TickerPriceTable.tickerId, TickerPriceTable.atr)
            .where { TickerPriceTable.tickerId inList ids }
            .associate { row ->
                row[TickerPriceTable.tickerId] to (row[TickerPriceTable.atr] ?: BigDecimal.ZERO)
            }
    }

    fun batchUpdateAtr(updates: Map<UUID, BigDecimal>) {
        updates.forEach { (id, newAtr) ->
            TickerPriceTable.update({ TickerPriceTable.id eq id }) {
                it[atr] = newAtr
            }
        }
    }

    fun existTickersByTickerCode(tickerCodes: List<String>) {
        val distinctCount = TickerTable.select(TickerTable.code)
            .where { TickerTable.code inList tickerCodes.distinct() }
            .distinct()
            .count()

        if (distinctCount != tickerCodes.size) {
            throw DomainPolicyViolationException("중복 혹은 유효하지 않은 종목 값으로 인해 수정에 실패했습니다.")
        }
    }
}