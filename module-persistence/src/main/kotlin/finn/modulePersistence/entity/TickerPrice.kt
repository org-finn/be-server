package finn.modulePersistence.entity

import finn.modulePersistence.table.TickerPriceTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class TickerPrice(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TickerPrice>(TickerPriceTable)

    var priceDate by TickerPriceTable.priceDate
    var open by TickerPriceTable.open
    var high by TickerPriceTable.high
    var low by TickerPriceTable.low
    var close by TickerPriceTable.close
    var volume by TickerPriceTable.volume
    var tickerCode by TickerPriceTable.tickerCode
    var ticker by Ticker referencedOn TickerPriceTable.tickerId
    var createdAt by TickerPriceTable.createdAt
}