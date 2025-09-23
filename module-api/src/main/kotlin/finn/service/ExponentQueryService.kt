package finn.service

import finn.repository.ExponentRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ExponentQueryService(
    private val exponentRepository: ExponentRepository
) {
    suspend fun getRecentExponent(code: String, priceDate: LocalDateTime): Double {
        return exponentRepository.getRecentExponentByCode(code, priceDate)
    }
}