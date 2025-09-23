package finn.repository

import java.time.LocalDateTime
import java.util.*

interface ExponentRepository {
    suspend fun getRealTimeRecentExponentByCode(exponentId: UUID, priceDate: LocalDateTime): Double
}