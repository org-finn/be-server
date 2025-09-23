package finn.repository

import java.time.LocalDateTime

interface ExponentRepository {
    suspend fun getRealTimeRecentExponentByCode(code: String, priceDate: LocalDateTime): Double
}