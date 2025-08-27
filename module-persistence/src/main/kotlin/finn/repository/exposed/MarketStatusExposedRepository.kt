package finn.repository.exposed

import finn.entity.MarketStatusExposed
import finn.table.MarketStatusTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class MarketStatusExposedRepository {

    fun findMarketStatusByDate(today: LocalDate): MarketStatusExposed? {
        return MarketStatusExposed.find(
            MarketStatusTable.date eq today
        ).singleOrNull()
    }
}