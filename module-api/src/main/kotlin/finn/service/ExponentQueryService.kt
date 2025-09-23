package finn.service

import finn.repository.ExponentRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class ExponentQueryService(
    private val exponentRepository: ExponentRepository
) {
    suspend fun getRecentExponent(exponentId: UUID, priceDate: LocalDateTime): Double {
        return exponentRepository.getRealTimeRecentExponentByCode(exponentId, priceDate)
    }
}