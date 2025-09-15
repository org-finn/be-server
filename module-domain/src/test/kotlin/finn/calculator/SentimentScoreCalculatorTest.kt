package finn.calculator

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

internal class SentimentScoreCalculatorTest : BehaviorSpec({

    /**
     * 시나리오 1: 뉴스 데이터가 없는 경우
     */
    Given("새로운 뉴스 데이터가 없을 때") {
        When("기존 점수도 없다면") {
            val score = calculateScore(
                recentScores = emptyList(),
                positiveArticleCount = 0,
                neutralArticleCount = 0,
                negativeArticleCount = 0
            )
            Then("기본값인 50점을 반환해야 한다") {
                score shouldBe 50
            }
        }

        When("기존 점수 [60, 70, 80]이 있다면") {
            val score = calculateScore(
                recentScores = listOf(60, 70, 80), // 평균: 70
                positiveArticleCount = 0,
                neutralArticleCount = 0,
                negativeArticleCount = 0
            )
            Then("기존 점수들의 평균값인 70점을 반환해야 한다") {
                score shouldBe 70
            }
        }
    }

    /**
     * 시나리오 2: 기존 점수 없이 뉴스 데이터만 있는 경우
     */
    Given("기존 점수는 없고 새로운 뉴스 데이터만 있을 때") {
        When("모든 뉴스가 긍정적이라면") {
            val score = calculateScore(
                recentScores = emptyList(),
                positiveArticleCount = 10,
                neutralArticleCount = 0,
                negativeArticleCount = 0
            )
            Then("정규화된 최고점인 100점을 반환해야 한다") {
                score shouldBe 100
            }
        }

        When("긍정과 부정 뉴스가 동일하다면") {
            val score = calculateScore(
                recentScores = emptyList(),
                positiveArticleCount = 10,
                neutralArticleCount = 5,
                negativeArticleCount = 10
            )
            Then("정규화된 중립 점수인 50점을 반환해야 한다") {
                score shouldBe 50
            }
        }
    }

    /**
     * 시나리오 3: 기존 점수와 뉴스 데이터가 모두 있는 경우 (핵심 로직)
     */
    Given("기존 점수와 새로운 뉴스 데이터가 모두 있을 때") {
        val recentScores = listOf(60, 80) // 오래된 순 -> 최신 순
        val positiveArticleCount = 10L
        val neutralArticleCount = 0L
        val negativeArticleCount = 0L
        // 이 경우, 정규화된 뉴스 점수는 100점

        When("calculateScore를 호출하면") {
            val score = calculateScore(
                recentScores = recentScores,
                positiveArticleCount = positiveArticleCount,
                neutralArticleCount = neutralArticleCount,
                negativeArticleCount = negativeArticleCount
            )

            Then("시간 순서에 따라 가중치가 적용된 평균 점수를 반환해야 한다") {
                // 수동 계산:
                // 가중 합계 = (60*1) + (80*2) + (100*3) = 60 + 160 + 300 = 520
                // 가중치 총합 = 1 + 2 + 3 = 6
                // 가중 평균 = 520 / 6 = 86.66... -> 반올림하여 87
                score shouldBe 87
            }
        }
    }
})
