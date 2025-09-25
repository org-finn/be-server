package finn.service

import finn.repository.ExponentRepository
import finn.task.ExponentPredictionTask.ExponentListPayload.ExponentPayload
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class ExponentQueryService(
    private val exponentRepository: ExponentRepository
) {
    suspend fun getRecentExponent(
        exponents: List<ExponentPayload>,
        priceDate: OffsetDateTime
    ) {
        exponentRepository.getRealTimeRecentExponentByCode(exponents, priceDate.toLocalDateTime())
    }
}