package finn.entity

import finn.table.MarketStatusTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class MarketStatusExposed(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MarketStatusExposed>(MarketStatusTable)

    var date by MarketStatusTable.date
    var tradingHours by MarketStatusTable.tradingHours
    var eventName by MarketStatusTable.eventName
}
