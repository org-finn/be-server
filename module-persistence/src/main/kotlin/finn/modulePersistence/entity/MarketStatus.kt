package finn.modulePersistence.entity

import finn.modulePersistence.table.MarketStatusTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class MarketStatus(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MarketStatus>(MarketStatusTable)

    var date by MarketStatusTable.date
    var tradingHours by MarketStatusTable.tradingHours
    var eventName by MarketStatusTable.eventName
}
