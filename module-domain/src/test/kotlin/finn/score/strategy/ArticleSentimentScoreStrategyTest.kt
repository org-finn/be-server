package finn.strategy

import finn.task.ArticlePredictionTask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.*

class ArticleSentimentScoreStrategyTest : BehaviorSpec({

    val strategy = ArticleSentimentScoreStrategy()

    fun createTask(
        pos: Long,
        neg: Long,
        neu: Long,
    ): ArticlePredictionTask {
        return ArticlePredictionTask(
            tickerId = UUID.randomUUID(),
            payload = ArticlePredictionTask.ArticlePayload(
                predictionDate = OffsetDateTime.now(),
                positiveArticleCount = pos,
                negativeArticleCount = neg,
                neutralArticleCount = neu,
                createdAt = OffsetDateTime.now()
            )
        )
    }

    // BehaviorSpec 구조: Given -> When -> Then
    Given("새로운 Stateless 점수 산정 전략은") {

        // [수정] Context -> When으로 변경
        When("수집된 기사가 하나도 없는 경우 (0건)") {
            val task = createTask(0, 0, 0)

            // [수정] When 내부에서 바로 Then 사용
            Then("사전 가중치(Prior)에 의해 정확히 중립 점수(50)를 반환해야 한다") {
                val result = strategy.calculate(task) // 메서드명 확인 필요 (calculateBatchScore or calculate)
                result shouldBe 50
            }
        }

        When("중립적인 기사만 다수 수집된 경우 (예: 20건)") {
            val task = createTask(0, 0, 20)

            Then("점수는 중립(50)이어야 한다") {
                val result = strategy.calculate(task)
                result shouldBe 50
            }
        }

        When("긍정적인 기사가 수집된 경우 (10건)") {
            val task = createTask(10, 0, 0)

            Then("점수는 50점을 초과해야 한다") {
                val result = strategy.calculate(task)
                result shouldBeGreaterThan 50
                println("Positive(10) Score: $result")
            }
        }

        When("긍정과 부정 기사 수가 동일한 경우 (예: 5 대 5)") {
            val task = createTask(5, 5, 0)

            Then("부정 가중치가 더 크므로 결과는 50점 미만이어야 한다") {
                val result = strategy.calculate(task)
                result shouldBeLessThan 50
            }
        }
    }
})