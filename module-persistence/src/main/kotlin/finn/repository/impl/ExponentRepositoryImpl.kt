package finn.repository.impl

import finn.repository.ExponentRepository
import finn.repository.dynamodb.ExponentRealTimeDynamoDbRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class ExponentRepositoryImpl(
    val exponentRealTimeDynamoDbRepository: ExponentRealTimeDynamoDbRepository
) : ExponentRepository {
    override suspend fun getRealTimeRecentExponentByCode(
        exponentId: UUID,
        priceDate: LocalDateTime
    ): Double {
        return exponentRealTimeDynamoDbRepository.findRealTimeRecentPriceData(exponentId, priceDate)
    }
}