package finn.entity

import finn.table.TickerTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class TickerExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TickerExposed>(TickerTable)

    var code by TickerTable.code
    var fullCompanyName by TickerTable.fullCompanyName
    var country by TickerTable.country
    var shortCompanyName by TickerTable.shortCompanyName
    var category by TickerTable.category
    var marketCap by TickerTable.marketCap
    var createdAt by TickerTable.createdAt
}