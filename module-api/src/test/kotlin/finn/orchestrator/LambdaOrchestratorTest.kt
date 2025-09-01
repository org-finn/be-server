import finn.entity.query.Ticker
import finn.orchestrator.LambdaOrchestrator
import finn.request.lambda.LambdaArticleRealTimeRequest
import finn.service.ArticleCommandService
import finn.service.PredictionCommandService
import finn.service.TickerQueryService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import java.time.OffsetDateTime
import java.util.*

internal class LambdaOrchestratorTest : BehaviorSpec({

    // 1. 의존성 Mocking
    val articleService =
        mockk<ArticleCommandService>(relaxed = true) // Unit을 반환하므로 relaxed = true 사용
    val predictionService = mockk<PredictionCommandService>(relaxed = true)
    val tickerService = mockk<TickerQueryService>()

    // 2. 테스트 대상(SUT) 인스턴스 생성
    val orchestrator = LambdaOrchestrator(articleService, predictionService, tickerService)

    // 각 테스트 케이스 실행 후 Mock 객체 초기화
    afterTest {
        clearMocks(articleService, predictionService, tickerService)
    }

    // --- 테스트 시나리오 ---

    Given("아티클 데이터가 없는 요청이 주어졌을 때") {
        val requestWithNoArticles = LambdaArticleRealTimeRequest(
            tickerCode = "AAPL",
            isMarketOpen = true,
            articles = emptyList(), // 아티클 리스트가 비어있음
            createdAt = OffsetDateTime.now(),
            predictionDate = OffsetDateTime.now()
        )

        When("saveArticleAndPrediction을 호출하면") {
            orchestrator.saveArticle(requestWithNoArticles)

            Then("아무 서비스도 호출되지 않고 조용히 종료되어야 한다") {
                // 어떤 서비스의 메서드도 호출되지 않았음을 검증
                verify(exactly = 0) { tickerService.getTickerByTickerCode(any()) }
                verify(exactly = 0) { articleService.saveArticleList(any()) }
                verify(exactly = 0) {
                    predictionService.savePrediction(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                    )
                }
            }
        }
    }

    Given("아티클 데이터가 있고 시장이 열려있을 때 (isMarketOpen = true)") {
        // 테스트용 데이터 준비
        val articleDto = LambdaArticleRealTimeRequest.LambdaArticle(
            title = "Apple's new product",
            description = "A new product was released.",
            thumbnailUrl = "url",
            articleUrl = "url",
            publishedDate = OffsetDateTime.now(),
            author = "Reuters",
            sentiment = "positive",
            reasoning = "Because it's new",
            distinctId = UUID.randomUUID().toString()
        )
        val request = LambdaArticleRealTimeRequest(
            tickerCode = "AAPL",
            isMarketOpen = true, // 시장이 열려있음
            articles = listOf(articleDto),
            createdAt = OffsetDateTime.now(),
            predictionDate = OffsetDateTime.now()
        )

        val mockTicker = Ticker.create(
            id = UUID.randomUUID(),
            tickerCode = "AAPL",
            shortCompanyName = "Apple",
            fullCompanyName = "Apple Inc."
        )

        // Mock 객체 동작 정의
        every { tickerService.getTickerByTickerCode("AAPL") } returns mockTicker

        When("saveArticleAndPrediction을 호출하면") {
            orchestrator.saveArticle(request)

            Then("Ticker, Article, Prediction 서비스가 순서대로 모두 호출되어야 한다") {
                // verifyOrder를 통해 메서드 호출 순서까지 검증
                verifyOrder {
                    tickerService.getTickerByTickerCode("AAPL")
                    articleService.saveArticleList(any())
                    predictionService.savePrediction(any(), any(), any(), any(), any())
                }
            }
        }
    }

    Given("아티클 데이터가 있지만 시장이 닫혀있을 때 (isMarketOpen = false)") {
        val articleDto = LambdaArticleRealTimeRequest.LambdaArticle(
            title = "Apple's new product",
            description = "A new product was released.",
            thumbnailUrl = "url",
            articleUrl = "url",
            publishedDate = OffsetDateTime.now(),
            author = "Reuters",
            sentiment = "positive",
            reasoning = "Because it's new",
            distinctId = UUID.randomUUID().toString()
        )
        val request = LambdaArticleRealTimeRequest(
            tickerCode = "MSFT",
            isMarketOpen = false, // 시장이 닫혀있음
            articles = listOf(articleDto),
            createdAt = OffsetDateTime.now(),
            predictionDate = OffsetDateTime.now()
        )
        val mockTicker = Ticker.create(
            id = UUID.randomUUID(),
            tickerCode = "MSFT",
            shortCompanyName = "Microsoft",
            fullCompanyName = "Microsoft Inc."
        )

        every { tickerService.getTickerByTickerCode("MSFT") } returns mockTicker

        When("saveArticleAndPrediction을 호출하면") {
            orchestrator.saveArticle(request)

            Then("Ticker와 Article 서비스는 호출되지만, Prediction 서비스는 호출되지 않아야 한다") {
                verify(exactly = 1) { tickerService.getTickerByTickerCode("MSFT") }
                verify(exactly = 1) { articleService.saveArticleList(any()) }
                // PredictionService는 호출되지 않았음을 검증
                verify(exactly = 0) {
                    predictionService.savePrediction(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                    )
                }
            }
        }
    }
})