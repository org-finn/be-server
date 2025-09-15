package finn.calculator

import kotlin.math.roundToInt

/**
 * 긍정 뉴스 개수(1), 중립 뉴스 개수(0), 부정 뉴스 개수(-1) -> 평균 값을 0~100 사이의 값으로 정규화
 * 오늘의 n분치 예측 데이터(윈도우 크기 한정)를 들고와서, 가중평균을 최종적으로 리턴
 */

fun calculateScore(
    recentScores: List<Int>,
    positiveArticleCount: Long,
    neutralArticleCount: Long,
    negativeArticleCount: Long
): Int {

    val totalArticleCount = positiveArticleCount + neutralArticleCount + negativeArticleCount

    // 뉴스가 없는 경우, 기존 점수들의 평균을 반환 (기존 점수도 없으면 중립 50점)
    if (totalArticleCount == 0L) {
        return if (recentScores.isEmpty()) 50 else recentScores.average().roundToInt()
    }

    // 긍정(+1), 부정(-1)을 기반으로 원시 점수(-1.0 ~ 1.0) 계산
    val rawArticleScore = (positiveArticleCount - negativeArticleCount).toDouble()
    val averageArticleScore = rawArticleScore / totalArticleCount

    // -1.0 ~ 1.0 범위를 0 ~ 100 범위로 정규화
    val normalizedArticleScore = ((averageArticleScore + 1) / 2) * 100

    // --- 2. 시간 순서에 따라 가중 평균 계산 ---

    // 2-1. 기존 점수(scores)의 가중 합계와 가중치 총합을 계산합니다.
    var weightedSumOfOldScores = 0.0
    var totalWeightOfOldScores = 0.0
    recentScores.forEachIndexed { index, score ->
        // 오래된 점수일수록 낮은 가중치(1)를, 최신 점수일수록 높은 가중치(scores.size)를 부여합니다.
        val weight = (index + 1).toDouble() // 가중치: 1, 2, 3, ...
        weightedSumOfOldScores += score * weight
        totalWeightOfOldScores += weight
    }

    // 2-2. 새로운 뉴스 점수는 가장 최신 데이터이므로 가장 높은 가중치를 부여합니다.
    val ArticleScoreWeight = (recentScores.size + 1).toDouble()

    // 2-3. 최종 가중 합계와 가중치 총합을 계산합니다.
    val finalWeightedSum = weightedSumOfOldScores + (normalizedArticleScore * ArticleScoreWeight)
    val finalTotalWeight = totalWeightOfOldScores + ArticleScoreWeight

    // 2-4. 가중 평균을 계산합니다.
    val finalScore = if (finalTotalWeight == 0.0) {
        // scores 리스트가 비어있으면 뉴스 점수가 유일한 데이터이므로 그대로 사용합니다.
        normalizedArticleScore
    } else {
        finalWeightedSum / finalTotalWeight
    }

    return finalScore.roundToInt()
}