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

            `when`("기사는 없지만 최근 점수만 존재하는 경우") {
                val task = PredictionTask(
                    tickerId = UUID.randomUUID(),
                    type = "article",
                    payload = mutableMapOf(
                        "positiveArticleCount" to 0,
                        "negativeArticleCount" to 0,
                        "neutralArticleCount" to 0,
                        "previousScore" to 80
                    )
                )

                then("오늘의 점수를 그대로 반환해야 한다.") {
                    val result = strategy.calculate(task)
                    result shouldBe 80
                }
            }
        }

        context("기사 데이터만으로 점수를 계산할 때") {
            `when`("긍정적인 기사만 1개가 있는 경우") {
                val task = PredictionTask(
                    tickerId = UUID.randomUUID(),
                    type = "article",
                    payload = mutableMapOf(
                        "positiveArticleCount" to 1,
                        "negativeArticleCount" to 0,
                        "neutralArticleCount" to 0,
                        "previousScore" to 50
                    )
                )

                then("계산 결과를 정확히 반영해야한다.") {
                    val result = strategy.calculate(task)
                    result shouldBe 53
                }
            }

            `when`("긍정적인 기사만 여러개가 있는 경우") {
                val task = PredictionTask(
                    tickerId = UUID.randomUUID(),
                    type = "article",
                    payload = mutableMapOf(
                        "positiveArticleCount" to 3,
                        "negativeArticleCount" to 0,
                        "neutralArticleCount" to 0,
                        "previousScore" to 50
                    )
                )

                then("계산 결과를 정확히 반영해야한다.") {
                    val result = strategy.calculate(task)
                    result shouldBe 62 // 3 + 4 + 5
                }
            }

            `when`("긍정과 부정 기사 수가 동일한 경우") {
                val task = PredictionTask(
                    tickerId = UUID.randomUUID(),
                    type = "article",
                    payload = mutableMapOf(
                        "positiveArticleCount" to 1,
                        "negativeArticleCount" to 1,
                        "neutralArticleCount" to 0,
                        "previousScore" to 50
                    )
                )

                then("현재 점수를 그대로 반환해야한다.") {
                    val result = strategy.calculate(task)
                    result shouldBe 50
                }
            }
        }

    }
})