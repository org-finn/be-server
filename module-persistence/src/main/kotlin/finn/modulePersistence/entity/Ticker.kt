package finn.modulePersistence.entity

import finn.modulePersistence.table.TickerTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Ticker(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Ticker>(TickerTable)

    var code by TickerTable.code
    var fullCompanyName by TickerTable.fullCompanyName
    var country by TickerTable.country
    var shortCompanyName by TickerTable.shortCompanyName
    var category by TickerTable.category
    var createdAt by TickerTable.createdAt
}