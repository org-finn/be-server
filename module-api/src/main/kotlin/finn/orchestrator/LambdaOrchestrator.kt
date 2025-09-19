package finn.orchestrator

import finn.entity.command.ArticleC
import finn.entity.command.ArticleInsight
import finn.handler.PredictionHandlerFactory
import finn.request.lambda.LambdaArticleRealTimeRequest
import finn.request.lambda.LambdaArticleRealTimeRequest.LambdaArticle
import finn.request.lambda.LambdaArticleRealTimeRequest.LambdaArticle.ArticleRealTimeInsightRequest
import finn.score.task.PredictionTask
import finn.service.ArticleCommandService
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
@ExposedTransactional(readOnly = true)
class LambdaOrchestrator(
    private val articleService: ArticleCommandService,
    private val handlerFactory: PredictionHandlerFactory
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    // SupervisorJob을 추가하여 특정 자식의 실패가 다른 자식들에게 영향을 주지 않도록 설정
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutexes = ConcurrentHashMap<String, Mutex>()

    @ExposedTransactional
    fun saveArticle(request: LambdaArticleRealTimeRequest) {
        // Article 생성 및 저장(Ticker_Article도 같이 생성)
        val article = createArticle(request.article)
        val insights = createArticleInsights(request.article.insights)
        articleService.saveArticleList(article, insights)
    }

    fun updatePrediction(task: PredictionTask) {
        val tickerId = task.tickerId
        val tickerMutex = mutexes.computeIfAbsent(tickerId.toString()) { Mutex() }
        val type = task.type

        scope.launch {
            tickerMutex.withLock {
                log.debug { "락 획득 성공. ${tickerId}: 예측을 수행합니다." }
                val handler = handlerFactory.findHandler(type)
                handler.handle(task)
                log.debug { "${tickerId}: 예측 수행완료. 락을 반납합니다." }
            }
        }
    }

    fun createArticle(article: LambdaArticle): ArticleC {
        return ArticleC.create(
            article.title,
            article.description,
            article.thumbnailUrl,
            article.articleUrl,
            article.publishedDate.toLocalDateTime(),
            article.author,
            article.distinctId,
            article.tickers,
        )
    }

    fun createArticleInsights(insights: List<ArticleRealTimeInsightRequest>): List<ArticleInsight> {
        return insights.map {
            ArticleInsight(
                it.tickerCode,
                it.sentiment,
                it.reasoning
            )
        }.toList()
    }

}