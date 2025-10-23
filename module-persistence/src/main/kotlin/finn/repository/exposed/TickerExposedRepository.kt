package finn.repository.exposed

import finn.entity.TickerExposed
import finn.exception.CriticalDataOmittedException
import finn.queryDto.TickerQueryDto
import finn.table.TickerPriceTable
import finn.table.TickerTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Repository
class TickerExposedRepository {

    private data class TickerQueryDtoImpl(
        val tickerId: UUID,
        val tickerCode: String,
        val shortCompanyName: String,
        val shortCompanyNameKr: String,
        val fullCompanyName: String
    ) : TickerQueryDto {
        override fun tickerId(): UUID = this.tickerId
        override fun tickerCode(): String = this.tickerCode
        override fun shortCompanyName(): String = this.shortCompanyName
        override fun shortCompanyNameKr(): String = this.shortCompanyNameKr
        override fun fullCompanyName(): String = this.fullCompanyName
    }

    fun findTickerListBySearchKeyword(keyword: String): List<TickerQueryDto> {
        return TickerTable.select(
            TickerTable.id,
            TickerTable.code,
            TickerTable.shortCompanyName,
            TickerTable.fullCompanyName
        ).where { TickerTable.shortCompanyName.lowerCase() like "${keyword.lowercase()}%" }
            .map { row ->
                TickerQueryDtoImpl(
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
            .map { row ->
                TickerQueryDtoImpl(
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
            .where { TickerPriceTable.tickerId eq tickerId }
            .limit(1)
            .map {
                it[TickerPriceTable.atr]
            }.singleOrNull()
            ?: throw CriticalDataOmittedException("최근 ${tickerId}의 ATR이 존재하지 않습니다.")
    }

    fun updateTodayAtrByTickerId(tickerId: UUID, todayAtr: BigDecimal) {
        TickerPriceTable.update({ (TickerPriceTable.tickerId eq tickerId) and (TickerPriceTable.priceDate.date() eq LocalDate.now()) }) {
            it[TickerPriceTable.atr] = todayAtr
        }
    }
}