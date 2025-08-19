package finn.entity

import finn.table.NIntervalChangeRateTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class NIntervalChangeRateExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<NIntervalChangeRateExposed>(NIntervalChangeRateTable)

    var interval by NIntervalChangeRateTable.interval
    var priceDate by NIntervalChangeRateTable.priceDate
    var changeRate by NIntervalChangeRateTable.changeRate
    var tickerCode by NIntervalChangeRateTable.tickerCode
    var tickerId by NIntervalChangeRateTable.tickerId
    var createdAt by NIntervalChangeRateTable.createdAt
}