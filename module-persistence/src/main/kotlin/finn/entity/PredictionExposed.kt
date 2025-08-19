package finn.entity

import finn.table.PredictionTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class PredictionExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PredictionExposed>(PredictionTable)

    var predictionDate by PredictionTable.predictionDate
    var positiveNewsCount by PredictionTable.positiveNewsCount
    var negativeNewsCount by PredictionTable.negativeNewsCount
    var neutralNewsCount by PredictionTable.neutralNewsCount
    val sentiment by PredictionTable.sentiment
    val strategy by PredictionTable.strategy
    var score by PredictionTable.score
    var tickerCode by PredictionTable.tickerCode
    var shortCompanyName by PredictionTable.shortCompanyName
    var tickerId by PredictionTable.tickerId
    var createdAt by PredictionTable.createdAt
}
