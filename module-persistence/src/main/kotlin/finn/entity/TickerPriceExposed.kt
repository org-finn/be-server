package finn.entity

import finn.table.TickerPriceTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class TickerPriceExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TickerPriceExposed>(TickerPriceTable)

    var priceDate by TickerPriceTable.priceDate
    var open by TickerPriceTable.open
    var high by TickerPriceTable.high
    var low by TickerPriceTable.low
    var close by TickerPriceTable.close
    var volume by TickerPriceTable.volume
    var atr by TickerPriceTable.atr
    var tickerCode by TickerPriceTable.tickerCode
    var tickerId by TickerExposed referencedOn TickerPriceTable.tickerId
    var createdAt by TickerPriceTable.createdAt
}