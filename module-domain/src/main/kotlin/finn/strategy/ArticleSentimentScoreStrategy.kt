package finn.strategy

import finn.task.ArticlePredictionTask
import org.springframework.stereotype.Component
import kotlin.math.ln
import kotlin.math.roundToInt

@Component
class ArticleSentimentScoreStrategy : SentimentScoreStrategy<ArticlePredictionTask> {

    companion object {
        // 점수 관찰 후 튜닝 필요
        private const val BASE_SCORE = 50.0 // 기본 점수 (중립)
        private const val PRIOR_WEIGHT = 3.0 // 사전 가중치 (이 값만큼의 '가상의 중립 기사'가 있다고 가정) -> 값이 클수록 점수 변화가 둔감함
        private const val EMA_ALPHA = 0.4 // 지수 이동 평균 반영 비율, 높을수록 최신 뉴스 반영이 빠름

        // 지나치게 낮은 로그 함수에 가중치 일부 보정
        private const val NEGATIVE_WEIGHT_MULTIPLIER = 6.0 // 부정 뉴스는 긍정 뉴스보다 상대적으로 시장 영향력이 높음을 반영, 가중치를 더 높게 설정
        private const val POSITIVE_WEIGHT_MULTIPLIER = 4.0
    }

    override fun supports(type: String): Boolean = type == "article"

    override suspend fun calculate(task: ArticlePredictionTask): Int {
        val posCount = task.payload.positiveArticleCount.toDouble()
        val negCount = task.payload.negativeArticleCount.toDouble()
        val neuCount = task.payload.neutralArticleCount.toDouble()
        val previousScore = task.payload.previousScore.toDouble()

        // 1. 로그 함수 적용 (수확 체감 법칙)
        // 기사가 많아질수록 1건당 증가폭 둔화
        val wPos = ln(1.0 + posCount) * POSITIVE_WEIGHT_MULTIPLIER
        val wNeg = ln(1.0 + negCount) * NEGATIVE_WEIGHT_MULTIPLIER
        val wNeu = ln(1.0 + neuCount)

        // 2. 베이지안 평균 계산 (0~100점 정규화)
        // 공식: (사전가중치 * 50점 + 긍정가중치 * 100점 + 부정가중치 * 0점 + 중립가중치 * 50점) / 전체가중치합
        val totalWeight = PRIOR_WEIGHT + wPos + wNeg + wNeu

        val weightedSum = (PRIOR_WEIGHT * BASE_SCORE) +
                (wPos * 100.0) +
                (wNeu * 50.0) +
                (wNeg * 0.0)

        val currentBatchScore = weightedSum / totalWeight

        // 3. 지수 이동 평균(EMA)으로 이전 점수와 결합
        // 이전 점수가 아예 없는 초기 상태(0점 등)라면 현재 점수를 바로 사용
        val finalScore = if (previousScore == 0.0) {
            currentBatchScore
        } else {
            (currentBatchScore * EMA_ALPHA) + (previousScore * (1.0 - EMA_ALPHA))
        }

        return finalScore.roundToInt().coerceIn(0, 100)
    }
}