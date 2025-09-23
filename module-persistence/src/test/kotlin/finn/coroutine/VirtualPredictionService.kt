package finn.coroutine

import finn.repository.PredictionRepository
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.*

class VirtualPredictionService(private val repository: PredictionRepository) {

    // 논블로킹 테스트를 위해 딜레이를 추가할 수 있는 가상 메서드
    suspend fun updatePredictionByArticleWithDelay(
        tickerId: UUID,
        predictionDate: LocalDateTime,
        positiveArticleCount: Long,
        negativeArticleCount: Long,
        neutralArticleCount: Long,
        score: Int,
        sentiment: Int,
        strategy: String,
        delayMillis: Long = 0
    ) {
        delay(delayMillis)
        repository.updatePredictionByArticle(
            tickerId, predictionDate, positiveArticleCount, negativeArticleCount,
            neutralArticleCount, score, sentiment, strategy
        )
    }

    // 롤백 테스트를 위해 예외를 발생시키는 가상 메서드
    suspend fun updatePredictionByArticleAndThrow(
        tickerId: UUID,
        predictionDate: LocalDateTime,
        positiveArticleCount: Long,
        negativeArticleCount: Long,
        neutralArticleCount: Long,
        score: Int,
        sentiment: Int,
        strategy: String
    ) {
        // 먼저 실제 DB 업데이트를 시도
        repository.updatePredictionByArticle(
            tickerId, predictionDate, positiveArticleCount, negativeArticleCount,
            neutralArticleCount, score, sentiment, strategy
        )
        // DB 업데이트 후, 서비스 로직에서 문제가 발생했다고 가정하고 예외를 던짐
        throw RuntimeException("서비스 로직 실패로 인한 롤백 시나리오!")
    }

}