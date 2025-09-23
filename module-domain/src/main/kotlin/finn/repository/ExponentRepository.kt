package finn.repository

import java.time.LocalDateTime

interface ExponentRepository {
    suspend fun getRecentExponentByCode(code: String, priceDate: LocalDateTime): Double
}