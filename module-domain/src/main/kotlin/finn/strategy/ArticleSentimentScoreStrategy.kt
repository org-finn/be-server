package finn.strategy

import finn.task.ArticlePredictionTask
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

/**
 * 긍정 뉴스 개수(1), 중립 뉴스 개수(0), 부정 뉴스 개수(-1) -> 평균 값을 0~100 사이의 값으로 정규화
 * 오늘의 n분치 예측 데이터(윈도우 크기 한정)를 들고와서, 가중평균을 최종적으로 리턴
 */

@Component
class ArticleSentimentScoreStrategy : SentimentScoreStrategy<ArticlePredictionTask> {

    override fun supports(type: String): Boolean = type == "article"

    override suspend fun calculate(task: ArticlePredictionTask): Int {
        val newPositiveArticleCount = task.payload.positiveArticleCount
        val newNegativeArticleCount = task.payload.negativeArticleCount
        val newNeutralArticleCount = task.payload.neutralArticleCount
        val previousScore = task.payload.previousScore

        var scoreChange = 0.0

        // 긍정 뉴스에 대한 선형적 점수 증가(개수 당 +1점)
        for (i in 0 until newPositiveArticleCount) {
            scoreChange += 1.0
        }

        // 부정 뉴스에 대한 비선형적 점수 감소
        // 1개: -5, 2개: -5-6=-11, 3개: -5-6-7=-18
        for (i in 0 until newNegativeArticleCount) {
            scoreChange -= (5.0 + i)
        }

        val newScore = previousScore + scoreChange

        // 점수는 항상 0과 100 사이의 값을 유지하도록 강제
        return newScore.coerceIn(0.0, 100.0).roundToInt()
    }
}
