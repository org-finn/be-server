package finn.entity

import finn.table.ExchangeRateTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ExchangeRateExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ExchangeRateExposed>(ExchangeRateTable)

    var date by ExchangeRateTable.date
    var indexInfo by ExchangeRateTable.indexInfo
    var changeRate by ExchangeRateTable.changeRate
    var createdAt by ExchangeRateTable.createdAt
}