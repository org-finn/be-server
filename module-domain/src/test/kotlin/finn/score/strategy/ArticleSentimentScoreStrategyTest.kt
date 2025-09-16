import finn.score.PredictionTask
import finn.score.strategy.ArticleSentimentScoreStrategy
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.*

class ArticleSentimentScoreStrategyTest : BehaviorSpec({

    // 테스트할 대상 인스턴스 생성
    val strategy = ArticleSentimentScoreStrategy()

    // given: 테스트의 큰 맥락을 설명
    given("ArticleSentimentScoreStrategy의 calculate 메서드는") {

        // context: 특정 상황을 설정
        context("입력 데이터가 비어있는 엣지 케이스를 다룰 때") {

            `when`("기사와 최근 점수가 모두 없는 경우") {
                val task = PredictionTask(
                    tickerId = UUID.randomUUID(),
                    type = "article",
                    payload = mutableMapOf(
                        "positiveArticleCount" to 0L,
                        "negativeArticleCount" to 0L,
                        "neutralArticleCount" to 0L,
                        "recentScores" to emptyList<Int>()
                    )
                )

                then("중립 점수인 50을 반환해야 한다.") {
                    val result = strategy.calculate(task)
                    result shouldBe 50
                }
            }

            `when`("기사는 없지만 최근 점수만 존재하는 경우") {
                val task = PredictionTask(
                    tickerId = UUID.randomUUID(),
                    type = "article",
                    payload = mutableMapOf(
                        "positiveArticleCount" to 0L,
                        "negativeArticleCount" to 0L,
                        "neutralArticleCount" to 0L,
                        "recentScores" to listOf(60, 70, 80) // 평균 70
                    )
                )

                then("최근 점수들의 단순 평균을 반환해야 한다.") {
                    val result = strategy.calculate(task)
                    result shouldBe 70
                }
            }
        }

        context("기사 데이터만으로 점수를 계산할 때") {
            `when`("긍정적인 기사만 있고 최근 점수가 없는 경우") {
                val task = PredictionTask(
                    tickerId = UUID.randomUUID(),
                    type = "article",
                    payload = mutableMapOf(
                        "positiveArticleCount" to 10L,
                        "negativeArticleCount" to 0L,
                        "neutralArticleCount" to 0L,
                        "recentScores" to emptyList<Int>()
                    )
                )

                then("정규화된 점수인 100을 반환해야 한다.") {
                    // (10-0)/10 = 1.0 -> ((1.0+1)/2)*100 = 100
                    val result = strategy.calculate(task)
                    result shouldBe 100
                }
            }

            `when`("긍정과 부정 기사 수가 동일하고 최근 점수가 없는 경우") {
                val task = PredictionTask(
                    tickerId = UUID.randomUUID(),
                    type = "article",
                    payload = mutableMapOf(
                        "positiveArticleCount" to 10L,
                        "negativeArticleCount" to 10L,
                        "neutralArticleCount" to 5L,
                        "recentScores" to emptyList<Int>()
                    )
                )

                then("정규화된 점수인 50을 반환해야 한다.") {
                    // (10-10)/25 = 0.0 -> ((0.0+1)/2)*100 = 50
                    val result = strategy.calculate(task)
                    result shouldBe 50
                }
            }
        }

        context("기사와 최근 점수를 종합하여 가중 평균을 계산할 때") {

            `when`("모든 데이터가 정상적으로 주어진 경우") {
                val task = PredictionTask(
                    tickerId = UUID.randomUUID(),
                    type = "article",
                    payload = mutableMapOf(
                        "positiveArticleCount" to 15L, // 긍정 15
                        "negativeArticleCount" to 5L,  // 부정 5
                        "neutralArticleCount" to 10L, // 중립 10
                        "recentScores" to listOf(60, 80) // 기존 점수
                    )
                )

                then("올바르게 가중 평균된 최종 점수를 반환해야 한다.") {
                    // 1. 기사 점수 계산
                    // raw = (15 - 5) / 30 = 0.333...
                    // normalized = ((0.333... + 1) / 2) * 100 = 66.666... -> 반올림시 67
                    val normalizedArticleScore = 66.666

                    // 2. 가중 평균 계산
                    // 기존 점수 가중합 = (60 * 1) + (80 * 2) = 220
                    // 기존 점수 가중치 총합 = 1 + 2 = 3
                    // 새 기사 점수 가중치 = 2 + 1 = 3
                    // 최종 가중합 = 220 + (66.666 * 3) = 220 + 200 = 420
                    // 최종 가중치 총합 = 3 + 3 = 6
                    // 최종 점수 = 420 / 6 = 70

                    val result = strategy.calculate(task)
                    result shouldBe 70
                }
            }
        }
    }
})