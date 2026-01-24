package finn.strategy

import finn.task.ArticlePredictionTask
import org.springframework.stereotype.Component
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * 이전 점수 기준으로 계산한 점수가 아닌 50점 기준으로 아예 새롭게 계산한 점수를 리턴
 * DB 단에서 RMA 공식에 따라 소폭 변동 반영
 */
@Component
class ArticleSentimentScoreStrategy : SentimentScoreStrategy<ArticlePredictionTask> {

    companion object {
        // 점수 관찰 후 튜닝 필요
        private const val BASE_SCORE = 50.0 // 기본 점수 (중립)
        private const val PRIOR_WEIGHT =
            3.0 // 사전 가중치 (이 값만큼의 '가상의 중립 기사'가 있다고 가정) -> 값이 클수록 점수 변화가 둔감함

        // 지나치게 낮은 로그 함수에 가중치 일부 보정
        private const val NEGATIVE_WEIGHT_MULTIPLIER =
            6.0 // 부정 뉴스는 긍정 뉴스보다 상대적으로 시장 영향력이 높음을 반영, 가중치를 더 높게 설정
        private const val POSITIVE_WEIGHT_MULTIPLIER = 4.0
    }

    override fun supports(type: String): Boolean = type == "article"

    override suspend fun calculate(task: ArticlePredictionTask): Int {
        val posCount = task.payload.positiveArticleCount.toDouble()
        val negCount = task.payload.negativeArticleCount.toDouble()
        val neuCount = task.payload.neutralArticleCount.toDouble()

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

        val rawBatchScore = weightedSum / totalWeight

        // 3. Int 변환 (반올림 및 0~100 범위 고정)
        return rawBatchScore.roundToInt().coerceIn(0, 100)
    }
}