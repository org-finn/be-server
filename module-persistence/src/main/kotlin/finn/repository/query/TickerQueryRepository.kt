package finn.repository.query

import finn.entity.TickerExposed
import finn.queryDto.TickerSearchQueryDto
import finn.table.TickerTable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class TickerQueryRepository {

    private data class TickerSearchQueryDtoImpl(
        val tickerId: UUID,
        val tickerCode: String,
        val shortCompanyName: String,
        val fullCompanyName: String
    ) : TickerSearchQueryDto {
        override fun tickerId(): UUID = this.tickerId
        override fun tickerCode(): String = this.tickerCode
        override fun shortCompanyName(): String = this.shortCompanyName
        override fun fullCompanyName(): String = this.fullCompanyName
    }

    fun findTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto> {
        return TickerTable.select(
            TickerTable.id,
            TickerTable.code,
            TickerTable.shortCompanyName,
            TickerTable.fullCompanyName
        ).where { TickerTable.shortCompanyName like "$keyword%" }
            .map { row ->
                TickerSearchQueryDtoImpl(
                    tickerId = row[TickerTable.id].value,
                    tickerCode = row[TickerTable.code],
                    shortCompanyName = row[TickerTable.shortCompanyName],
                    fullCompanyName = row[TickerTable.fullCompanyName]
                )
            }
    }

    fun findByTickerCode(tickerCode: String): TickerExposed {
        return TickerExposed.find { TickerTable.code eq tickerCode }
            .single()
    }
}