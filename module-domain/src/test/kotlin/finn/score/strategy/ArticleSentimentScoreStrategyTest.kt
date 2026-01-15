import finn.strategy.ArticleSentimentScoreStrategy
import finn.task.ArticlePredictionTask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.*

class ArticleSentimentScoreStrategyTest : BehaviorSpec({

    val strategy = ArticleSentimentScoreStrategy()

    // 테스트 헬퍼 함수: 보일러플레이트 코드 감소
    fun createTask(
        pos: Long,
        neg: Long,
        neu: Long,
        prevScore: Int
    ): ArticlePredictionTask {
        val task = ArticlePredictionTask(
            tickerId = UUID.randomUUID(),
            payload = ArticlePredictionTask.ArticlePayload(
                predictionDate = OffsetDateTime.now(),
                positiveArticleCount = pos,
                negativeArticleCount = neg,
                neutralArticleCount = neu,
                createdAt = OffsetDateTime.now()
            )
        )
        task.payload.previousScore = prevScore
        return task
    }

    given("베이지안 평균과 로그 함수가 적용된 ScoreStrategy는") {

        // 변경점 1: 기사가 0개여도 예외를 던지지 않고, 베이지안 사전(Prior) 분포에 의해 중립(50점)으로 수렴해야 함
        Context("수집된 기사가 하나도 없는 경우 (0건)") {
            `when`("이전 점수가 80점(긍정적)이었다면") {
                val task = createTask(0, 0, 0, 80)

                then("점수는 중립(50점) 방향으로 서서히 하락해야 한다 (EMA 적용)") {
                    val result = strategy.calculate(task)
                    // EMA(0.3) 적용 시: (50 * 0.3) + (80 * 0.7) ≈ 71
                    result shouldBeLessThan 80
                    result shouldBeGreaterThan 50
                }
            }

            `when`("이전 점수가 0점(초기 상태)이었다면") {
                val task = createTask(0, 0, 0, 0)

                then("기본 베이지안 평균인 50점(중립)이 되어야 한다") {
                    val result = strategy.calculate(task)
                    result shouldBe 50
                }
            }
        }

        Context("기사 데이터가 존재할 때") {

            // 변경점 2: 중립 기사가 많아도 '고정'이 아니라 '50점으로 수렴'해야 함
            `when`("중립 기사의 비율이 압도적으로 높은 경우") {
                val task = createTask(1, 1, 20, 80) // 긍/부정은 적고 중립만 많음, 기존 점수 높음

                then("기존 점수(80)보다 낮아져 중립(50)에 가까워져야 한다") {
                    val result = strategy.calculate(task)
                    result shouldBeLessThan 80
                    result shouldBeGreaterThan 50
                }
            }

            `when`("긍정적인 기사가 다수 수집된 경우") {
                val task = createTask(10, 0, 0, 50)

                then("점수는 상승해야 한다") {
                    val result = strategy.calculate(task)
                    result shouldBeGreaterThan 50
                }
            }

            // 변경점 3: 로그 함수 적용으로 인한 포화(Saturation) 검증
            `when`("긍정 기사가 폭발적으로 많은 경우 (100건)") {
                val task = createTask(100, 0, 0, 50)

                then("점수는 상승하되 강한 폭으로 상승해서는 안 된다") {
                    val result = strategy.calculate(task)
                    result shouldBeLessThan 80 // 적당히 약한 호재에 머물러야함
                    print(result)
                }
            }

            // 변경점 4: 부정 편향(Negative Bias) 검증
            `when`("긍정과 부정 기사 수가 동일한 경우 (예: 5 대 5)") {
                val task = createTask(5, 5, 0, 50)

                then("부정 가중치가 더 크므로 50점보다 낮아져야 한다") {
                    val result = strategy.calculate(task)
                    result shouldBeLessThan 50
                }
            }
        }
    }
})