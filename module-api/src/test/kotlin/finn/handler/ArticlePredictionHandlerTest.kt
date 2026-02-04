package finn.handler

import finn.converter.SentimentConverter
import finn.entity.query.PredictionQ
import finn.entity.query.PredictionStrategy
import finn.queryDto.PredictionUpdateDto
import finn.service.PredictionCommandService
import finn.service.PredictionQueryService
import finn.strategy.ArticleSentimentScoreStrategy
import finn.strategy.StrategyFactory
import finn.task.ArticlePredictionTask
import finn.task.PredictionTask
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

class ArticlePredictionHandlerTest : BehaviorSpec({

    // 1. Mocks
    val commandService = mockk<PredictionCommandService>(relaxed = true)
    val queryService = mockk<PredictionQueryService>()
    val sentimentConverter = mockk<SentimentConverter>()
    val strategyFactory = mockk<StrategyFactory>()

    // 2. Real Object
    val realStrategy = ArticleSentimentScoreStrategy()

    // 3. System Under Test
    val handler = ArticlePredictionHandler(
        commandService,
        sentimentConverter,
        queryService,
        strategyFactory
    )

    beforeTest {
        clearAllMocks() // 모든 Mock 객체의 호출 기록과 설정 초기화
        every { strategyFactory.findSentimentScoreStrategy("article") } returns realStrategy
        every { sentimentConverter.getStrategyFromScore(any()) } returns PredictionStrategy.STRONG_BUY
        every { sentimentConverter.getSentiment(any()) } returns 1
    }

    fun createTask(tickerId: UUID, pos: Long, neg: Long, neu: Long): ArticlePredictionTask {
        return ArticlePredictionTask(
            tickerId = tickerId,
            payload = ArticlePredictionTask.ArticlePayload(
                predictionDate = OffsetDateTime.now(),
                positiveArticleCount = pos,
                negativeArticleCount = neg,
                neutralArticleCount = neu,
                createdAt = OffsetDateTime.now()
            )
        )
    }

    Given("기사 예측 핸들러(Handler)는") {
        val tickerId = UUID.randomUUID()
        val predictionDate = LocalDateTime.now()

        When("정상적인 기사 태스크 목록이 들어왔을 때") {
            val initialScore = 50
            val tasks = listOf(createTask(tickerId, 10, 0, 0))

            Then("전략을 실행하여 점수를 계산하고 업데이트 요청을 해야 한다") {

                coEvery { queryService.findAllByTickerIdsForPrediction(any()) } returns listOf(
                    PredictionQ.create(
                        tickerId = tickerId,
                        tickerCode = "005930",
                        shortCompanyName = "Samsung",
                        predictionDate = predictionDate,
                        sentimentScore = initialScore,
                        positiveArticleCount = 0,
                        negativeArticleCount = 0,
                        neutralArticleCount = 0,
                        sentiment = 0,
                        strategy = PredictionStrategy.NEUTRAL.strategy,
                    )
                )

                handler.handle(tasks)

                val updatesSlot = slot<List<PredictionUpdateDto>>()
                val alphaSlot = slot<Double>()

                coVerify(exactly = 1) {
                    commandService.updatePredictions(capture(updatesSlot), capture(alphaSlot))
                }

                val update = updatesSlot.captured.first()
                update.positiveArticleCount shouldBe 10
            }
        }

        When("여러 건의 태스크가 한 번에 들어왔을 때 (Aggregation)") {
            val task1 = createTask(tickerId, 10, 0, 0)
            val task2 = createTask(tickerId, 0, 10, 0)
            val tasks = listOf(task1, task2)

            Then("순차적으로 점수를 반영하여 누적 업데이트해야 한다") {
                coEvery { queryService.findAllByTickerIdsForPrediction(any()) } returns listOf(
                    PredictionQ.create(
                        tickerId = tickerId,
                        tickerCode = "Test",
                        shortCompanyName = "Test",
                        predictionDate = predictionDate,
                        sentimentScore = 50,
                        positiveArticleCount = 0, negativeArticleCount = 0, neutralArticleCount = 0,
                        sentiment = 0,
                        strategy = PredictionStrategy.NEUTRAL.strategy,
                    )
                )

                handler.handle(tasks)

                val updatesSlot = slot<List<PredictionUpdateDto>>()
                coVerify { commandService.updatePredictions(capture(updatesSlot), any()) }

                val update = updatesSlot.captured.first()
                update.positiveArticleCount shouldBe 10
                update.negativeArticleCount shouldBe 10
            }
        }

        When("DB에 해당 종목의 데이터가 없을 때") {
            val tasks = listOf(createTask(tickerId, 1, 0, 0))
            coEvery { queryService.findAllByTickerIdsForPrediction(any()) } returns emptyList()

            Then("RuntimeException을 던져야 한다") {
                shouldThrow<RuntimeException> {
                    handler.handle(tasks)
                }
            }
        }

        // [문제의 테스트 케이스]
        When("빈 태스크 리스트 혹은 처리할 수 없는 타입이 들어왔을 때") {
            val emptyTasks = emptyList<PredictionTask>()

            Then("db 쓰기 작업은 수행하지 않아야 한다") {
                handler.handle(emptyTasks)

                // clearAllMocks() 덕분에 이전 테스트의 기록이 사라져서 성공함
                coVerify(exactly = 0) { commandService.updatePredictions(any(), any()) }
            }
        }

        When("이미 DB에 카운트가 100개 쌓여있는 상태에서, 새로운 기사 5건이 들어왔을 때") {
            // Given: 기존 DB 상태 (Pos: 100)
            val existingPosCount = 100L
            val newPosCount = 5L

            val tasks = listOf(createTask(tickerId, newPosCount, 0, 0)) // +5 요청

            Then("CommandService에는 합산된 값(105)이 아니라, '추가될 값(5)'만 전달되어야 한다") {
                // Mocking: QueryService가 기존값 100을 리턴
                coEvery { queryService.findAllByTickerIdsForPrediction(any()) } returns listOf(
                    PredictionQ.create(
                        tickerId = tickerId,
                        tickerCode = "TEST",
                        shortCompanyName = "TEST",
                        predictionDate = predictionDate,
                        sentimentScore = 50,
                        positiveArticleCount = existingPosCount, // 100
                        negativeArticleCount = 0,
                        neutralArticleCount = 0,
                        sentiment = 0,
                        strategy = PredictionStrategy.NEUTRAL.strategy,
                    )
                )

                handler.handle(tasks)

                val updatesSlot = slot<List<PredictionUpdateDto>>()
                coVerify { commandService.updatePredictions(capture(updatesSlot), any()) }

                val update = updatesSlot.captured.first()

                println("=============================================")
                println("DB Existing : $existingPosCount")
                println("New Input   : $newPosCount")
                println("Sent to DB  : ${update.positiveArticleCount}")
                println("=============================================")

                // [검증 포인트]
                // 만약 핸들러가 '기존값 + 신규값'을 보낸다면 update.positiveArticleCount는 105가 됩니다.
                // 하지만 SQL이 'set count = count + ?'라면, 여기엔 오직 5(Delta)만 있어야 합니다.

                // 버그가 있다면 이 단언문(Assertion)에서 실패할 것입니다.
                update.positiveArticleCount shouldBe newPosCount
            }
        }
    }


})