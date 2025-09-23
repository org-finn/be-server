package finn.entity

import finn.entity.query.PredictionStrategy
import java.util.*
import kotlin.properties.Delegates

data class TickerScore(
    val tickerId: UUID,
    val score: Int
) {
    var sentiment: Int by Delegates.notNull()
    var strategy: PredictionStrategy by Delegates.notNull()
}