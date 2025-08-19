package finn.modulePersistence.entity

import finn.modulePersistence.table.PredictionTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Prediction(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Prediction>(PredictionTable)

    var predictionDate by PredictionTable.predictionDate
    var positiveNewsCount by PredictionTable.positiveNewsCount
    var negativeNewsCount by PredictionTable.negativeNewsCount
    var neutralNewsCount by PredictionTable.neutralNewsCount
    var score by PredictionTable.score
    var tickerCode by PredictionTable.tickerCode
    var shortCompanyName by PredictionTable.shortCompanyName
    var ticker by Ticker referencedOn PredictionTable.tickerId
    var createdAt by PredictionTable.createdAt
}
