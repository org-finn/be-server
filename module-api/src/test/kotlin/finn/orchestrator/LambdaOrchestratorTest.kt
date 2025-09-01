import finn.entity.command.ArticleC
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

    // 의존성 Mocking
    val articleService = mockk<ArticleCommandService>(relaxed = true)
    val predictionService = mockk<PredictionCommandService>(relaxed = true)
    val tickerService = mockk<TickerQueryService>()
    mockkObject(ArticleC.Companion)

    // 테스트 대상(SUT) 인스턴스 생성
    val orchestrator = LambdaOrchestrator(articleService, predictionService, tickerService)

    afterEach {
        clearMocks(articleService, predictionService, tickerService)
        clearStaticMockk(ArticleC::class)
    }

    // --- saveArticle 메서드 테스트 ---
    Context("saveArticle 메서드는") {
        Given("아티클 데이터가 없는 요청이 주어졌을 때") {
            // 1. 새로운 Request DTO 구조에 맞게 테스트 데이터 생성
            val requestWithNoArticles = LambdaArticleRealTimeRequest(
                tickerCode = "AAPL",
                articles = emptyList(),
                isMarketOpen = true,
                predictionDate = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            When("saveArticle을 호출하면") {
                orchestrator.saveArticle(requestWithNoArticles)

                Then("아무 서비스도 호출하지 않고 종료해야 한다") {
                    verify(exactly = 0) { tickerService.getTickerByTickerCode(any()) }
                    verify(exactly = 0) { articleService.saveArticleList(any()) }
                }
            }
        }

        Given("특정 종목(AAPL)에 대한 뉴스 요청이 주어졌을 때") {
            // 2. 새로운 Request DTO 구조에 맞게 테스트 데이터 생성
            val articleDto = LambdaArticleRealTimeRequest.LambdaArticle(
                publishedDate = OffsetDateTime.now(),
                title = "Apple's new iPhone announced",
                description = "Details about the new iPhone.",
                articleUrl = "https://example.com/news/aapl-1",
                thumbnailUrl = "https://example.com/thumb/aapl-1.jpg",
                author = "Tech News",
                distinctId = "distinct-id-123",
                sentiment = "positive",
                reasoning = "New features are promising."
            )
            val request = LambdaArticleRealTimeRequest(
                tickerCode = "AAPL",
                articles = listOf(articleDto),
                isMarketOpen = true, // isMarketOpen 로직은 savePrediction으로 이동
                predictionDate = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )
            val mockTicker = Ticker.create(
                id = UUID.randomUUID(), tickerCode = "AAPL", shortCompanyName = "Apple", fullCompanyName = "Apple Inc."
            )

            every { ArticleC.isNotProcessingPredictionArticles("AAPL") } returns false
            every { tickerService.getTickerByTickerCode("AAPL") } returns mockTicker

            When("saveArticle을 호출하면") {
                orchestrator.saveArticle(request)

                Then("tickerService와 articleService를 순서대로 호출해야 한다") {
                    verifyOrder {
                        tickerService.getTickerByTickerCode("AAPL")
                        articleService.saveArticleList(any())
                    }
                }
            }
        }
    }

    // ... savePrediction 메서드 테스트 ...
})