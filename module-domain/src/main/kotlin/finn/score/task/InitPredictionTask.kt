package finn.score.task

import java.time.OffsetDateTime
import java.util.*

data class InitPredictionTask(
    override val tickerId: UUID,
    val payload: InitPayload
) : PredictionTask() {
    override val type: String = "init"

    data class InitPayload(
        val tickerCode: String,
        val shortCompanyName: String,
        val predictionDate: OffsetDateTime,
        val todayMacd: Map<String, Double>,
        val yesterdayMacd: Map<String, Double>,
        val todayMa: Map<String, Double>,
        val yesterdayMa: Map<String, Double>,
        val todayRsi: Double,
        val createdAt: OffsetDateTime,
    ) {
        lateinit var recentScores: List<Int>
    }

}
