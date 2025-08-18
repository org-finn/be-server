package finn.moduleDomain.entity

import finn.moduleDomain.calculator.SentimentScoreCalculator
import finn.moduleDomain.exception.BadRequestDomainPolicyViolationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.*

internal class PredictionTest : BehaviorSpec({

    // 테스트에 사용할 mock SentimentScoreCalculator(격리성 보장)
    val mockCalculator = mockk<SentimentScoreCalculator>()

    // 테스트용 공통 데이터
    val testTickerId = UUID.randomUUID()
    val testTickerCode = "AAPL"
    val testCompanyName = "Apple"
    val testDate = LocalDateTime.now()

    /**
     * 1. create 팩토리 메서드 테스트
     */
    Given("Prediction.create 메서드를 호출할 때") {
        val positiveCount = 10
        val negativeCount = 2
        val neutralCount = 5
        val todayScores = listOf(60, 65, 70)
        val calculatedScore = 85 // 계산기가 반환할 가짜 점수

        // mockCalculator의 동작을 정의: 어떤 값이 들어가든 85를 반환하도록 설정
        every {
            mockCalculator.calculateScore(
                any(), any(),
                any(), any(),
                any(), any()
            )
        } returns calculatedScore

        When("필요한 모든 정보와 계산기를 전달하면") {
            val prediction = Prediction.create(
                tickerId = testTickerId,
                tickerCode = testTickerCode,
                shortCompanyName = testCompanyName,
                positiveNewsCount = positiveCount,
                negativeNewsCount = negativeCount,
                neutralNewsCount = neutralCount,
                predictionDate = testDate,
                collectedDate = testDate,
                todayScores = todayScores,
                calculator = mockCalculator
            )

            Then("계산된 점수와 그에 맞는 전략으로 Prediction 객체가 생성되어야 한다") {
                prediction.tickerCode shouldBe testTickerCode
                prediction.sentimentScore shouldBe calculatedScore // mock이 반환한 값
                prediction.predictionStrategy shouldBe PredictionStrategy.STRONG_BUY // 85점에 해당하는 전략
            }
        }
    }

    /**
     * 2. getStrategyFromScore 함수 테스트
     */
    Given("getStrategyFromScore 함수에 점수를 전달할 때") {
        withData(
            nameFn = { (score, strategy) -> "$score 점은 ${strategy.name} 전략으로 매핑되어야 한다" },
            85 to PredictionStrategy.STRONG_BUY,
            70 to PredictionStrategy.WEEK_BUY,
            50 to PredictionStrategy.NEUTRAL,
            30 to PredictionStrategy.WEEK_SELL,
            10 to PredictionStrategy.STRONG_SELL
        ) { (score, expectedStrategy) ->
            val actualStrategy = Prediction.getStrategyFromScore(score)
            actualStrategy shouldBe expectedStrategy
        }

        When("유효하지 않은 점수(-10)를 전달하면") {
            Then("예외가 발생해야 한다") {
                shouldThrow<BadRequestDomainPolicyViolationException> {
                    Prediction.getStrategyFromScore(-10)
                }
            }
        }
    }

    /**
     * 3. getNewsCountAlongWithStrategy 인스턴스 메서드 테스트
     */
    Given("생성된 Prediction 객체의 투자 전략에 따라") {
        When("전략이 STRONG_BUY일 때 getNewsCountAlongWithStrategy를 호출하면") {
            // STRONG_BUY 전략(81~100점)을 갖도록 90점을 반환하는 mock 설정
            every {
                mockCalculator.calculateScore(
                    any(),
                    any(),
                    any(),
                    15,
                    any(),
                    any()
                )
            } returns 90
            val prediction = Prediction.create(
                // ... 다른 파라미터들
                positiveNewsCount = 15,
                negativeNewsCount = 1,
                neutralNewsCount = 3,
                // 아래 파라미터들은 테스트에 영향을 주지 않으므로 간단히 채움
                tickerId = testTickerId,
                tickerCode = testTickerCode,
                shortCompanyName = testCompanyName,
                predictionDate = testDate,
                collectedDate = testDate,
                todayScores = emptyList(),
                calculator = mockCalculator
            )

            Then("긍정 뉴스 개수(positiveNewsCount)를 반환해야 한다") {
                prediction.getNewsCountAlongWithStrategy() shouldBe 15
            }
        }

        When("전략이 NEUTRAL일 때 getNewsCountAlongWithStrategy를 호출하면") {
            // NEUTRAL 전략(41~60점)을 갖도록 50점을 반환하는 mock 설정
            every {
                mockCalculator.calculateScore(
                    any(),
                    any(),
                    any(),
                    any(),
                    10,
                    any()
                )
            } returns 50
            val prediction = Prediction.create(
                positiveNewsCount = 5,
                negativeNewsCount = 5,
                neutralNewsCount = 10,
                tickerId = testTickerId,
                tickerCode = testTickerCode,
                shortCompanyName = testCompanyName,
                predictionDate = testDate,
                collectedDate = testDate,
                todayScores = emptyList(),
                calculator = mockCalculator
            )
            Then("중립 뉴스 개수(neutralNewsCount)를 반환해야 한다") {
                prediction.getNewsCountAlongWithStrategy() shouldBe 10
            }
        }
    }
})