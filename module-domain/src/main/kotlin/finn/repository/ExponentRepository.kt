package finn.repository

import finn.task.ExponentPredictionTask.ExponentListPayload.ExponentPayload
import java.time.LocalDateTime

interface ExponentRepository {
    suspend fun getRealTimeRecentExponentByCode(
        exponents: List<ExponentPayload>,
        priceDate: LocalDateTime
    )
}