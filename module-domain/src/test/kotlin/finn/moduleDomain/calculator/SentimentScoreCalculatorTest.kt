package finn.moduleDomain.calculator

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

internal class SentimentScoreCalculatorTest : BehaviorSpec({

    // 테스트 대상 인스턴스 생성
    val calculator = SentimentScoreCalculator()
    val testTickerCode = "AAPL"
    val testDate = LocalDateTime.now()

    /**
     * 시나리오 1: 뉴스 데이터가 없는 경우
     */
    Given("새로운 뉴스 데이터가 없을 때") {
        When("기존 점수도 없다면") {
            val score = calculator.calculateScore(
                tickerCode = testTickerCode,
                collectedDate = testDate,
                todayScores = emptyList(),
                positiveNewsCount = 0,
                neutralNewsCount = 0,
                negativeNewsCount = 0
            )
            Then("기본값인 50점을 반환해야 한다") {
                score shouldBe 50
            }
        }

        When("기존 점수 [60, 70, 80]이 있다면") {
            val score = calculator.calculateScore(
                tickerCode = testTickerCode,
                collectedDate = testDate,
                todayScores = listOf(60, 70, 80), // 평균: 70
                positiveNewsCount = 0,
                neutralNewsCount = 0,
                negativeNewsCount = 0
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
            val score = calculator.calculateScore(
                tickerCode = testTickerCode,
                collectedDate = testDate,
                todayScores = emptyList(),
                positiveNewsCount = 10,
                neutralNewsCount = 0,
                negativeNewsCount = 0
            )
            Then("정규화된 최고점인 100점을 반환해야 한다") {
                score shouldBe 100
            }
        }

        When("긍정과 부정 뉴스가 동일하다면") {
            val score = calculator.calculateScore(
                tickerCode = testTickerCode,
                collectedDate = testDate,
                todayScores = emptyList(),
                positiveNewsCount = 10,
                neutralNewsCount = 5,
                negativeNewsCount = 10
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
        val todayScores = listOf(60, 80) // 오래된 순 -> 최신 순
        val positiveNewsCount = 10
        val neutralNewsCount = 0
        val negativeNewsCount = 0
        // 이 경우, 정규화된 뉴스 점수는 100점

        When("calculateScore를 호출하면") {
            val score = calculator.calculateScore(
                tickerCode = testTickerCode,
                collectedDate = testDate,
                todayScores = todayScores,
                positiveNewsCount = positiveNewsCount,
                neutralNewsCount = neutralNewsCount,
                negativeNewsCount = negativeNewsCount
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
