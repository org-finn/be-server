import finn.entity.command.ArticleC
import finn.orchestrator.ArticleOrchestrator
import finn.request.lambda.LambdaArticleRealTimeRequest
import finn.service.ArticleCommandService
import finn.service.ArticleQueryService
import finn.service.TickerQueryService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import java.time.OffsetDateTime

internal class ArticleOrchestratorTest : BehaviorSpec({

    // 의존성 Mocking
    val articleQueryService = mockk<ArticleQueryService>(relaxed = true)
    val articleCommandService = mockk<ArticleCommandService>(relaxed = true)
    val tickerQueryService = mockk<TickerQueryService>(relaxed = true)
    mockkObject(ArticleC.Companion)

    // 테스트 대상(SUT) 인스턴스 생성
    val orchestrator =
        ArticleOrchestrator(articleCommandService, articleQueryService, tickerQueryService)

    afterEach {
        clearMocks(articleQueryService, articleCommandService, tickerQueryService)
        clearStaticMockk(ArticleC::class)
    }

    // --- saveArticle 메서드 테스트 ---
    Context("saveArticle 메서드는") {

        // 테스트 데이터 생성
        val insightRequest =
            LambdaArticleRealTimeRequest.LambdaArticle.ArticleRealTimeInsightRequest(
                tickerCode = "AAPL", sentiment = "positive", reasoning = "New features"
            )
        val articleRequest = LambdaArticleRealTimeRequest.LambdaArticle(
            publishedDate = OffsetDateTime.now(),
            title = "Test Article",
            description = "Test Description",
            articleUrl = "http://example.com/1",
            thumbnailUrl = null,
            author = "Test Author",
            distinctId = "test-id-1",
            tickers = listOf("AAPL", "MSFT"),
            insights = listOf(insightRequest)
        )
        val request = LambdaArticleRealTimeRequest(
            article = articleRequest,
            isMarketOpen = true,
            createdAt = OffsetDateTime.now(),
        )

        Given("아티클 1개가 들어올때") {
            // Mocking: ArticleC.create는 어떤 ArticleC 객체를 반환한다고 가정
            every {
                ArticleC.create(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns mockk()

            When("saveArticle을 호출하면") {
                orchestrator.saveArticle(request)

                Then("articleService.saveArticleList를 호출해야 한다") {
                    verify(exactly = 1) { articleCommandService.saveArticleList(any(), any()) }
                }
            }
        }
    }

})