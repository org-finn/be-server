package finn.repository.exposed

import finn.entity.MarketStatusExposed
import finn.table.MarketStatusTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class MarketStatusExposedRepository {

    fun findMarketStatusByDate(today: LocalDate): MarketStatusExposed? {
        return MarketStatusTable.selectAll()
            .where(MarketStatusTable.date eq today)
            .singleOrNull()?.let { MarketStatusExposed.wrapRow(it) }
    }
}