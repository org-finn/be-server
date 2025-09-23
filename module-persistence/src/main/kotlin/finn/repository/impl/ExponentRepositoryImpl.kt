package finn.repository.impl

import finn.repository.ExponentRepository
import finn.repository.dynamodb.ExponentRealTimeDynamoDbRepository
import finn.task.ExponentPredictionTask.ExponentListPayload.ExponentPayload
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ExponentRepositoryImpl(
    val exponentRealTimeDynamoDbRepository: ExponentRealTimeDynamoDbRepository
) : ExponentRepository {
    override suspend fun getRealTimeRecentExponentByCode(
        exponents: List<ExponentPayload>,
        priceDate: LocalDateTime
    ) {
        exponentRealTimeDynamoDbRepository.findAndSetPreviousValues(exponents, priceDate)
    }
}