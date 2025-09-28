package finn.orchestrator

import finn.entity.command.ArticleC
import finn.entity.command.ArticleInsight
import finn.handler.PredictionHandlerFactory
import finn.lock.CoroutineReadWriteLock
import finn.request.lambda.LambdaArticleRealTimeRequest
import finn.request.lambda.LambdaArticleRealTimeRequest.LambdaArticle
import finn.request.lambda.LambdaArticleRealTimeRequest.LambdaArticle.ArticleRealTimeInsightRequest
import finn.service.ArticleCommandService
import finn.task.PredictionTask
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class LambdaOrchestrator(
    private val articleService: ArticleCommandService,
    private val handlerFactory: PredictionHandlerFactory,
    private val coroutineReadWriteLock: CoroutineReadWriteLock
) {
    companion object {
        private val log = KotlinLogging.logger {}
        private val wildcard: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 개별 티커 간의 충돌을 막기 위한 Mutex
    private val mutexes = ConcurrentHashMap<UUID, Mutex>()

    @ExposedTransactional
    fun saveArticle(request: LambdaArticleRealTimeRequest) {
        // Article 생성 및 저장(Ticker_Article도 같이 생성)
        val article = createArticle(request.article)
        val insights = createArticleInsights(request.article.insights)
        articleService.saveArticleList(article, insights)
    }

    fun updatePrediction(task: PredictionTask) {
        val tickerId = task.tickerId
        val type = task.type
        val handler = handlerFactory.findHandler(type)
        scope.launch {
            if (tickerId == wildcard) {
                // 와일드카드(*) 작업: 쓰기 락(Write Lock)을 사용
                log.info { "글로벌 작업(*) 시작. 쓰기 락 획득 시도..." }
                coroutineReadWriteLock.write {
                    log.info { "쓰기 락 획득 성공. 모든 개별 티커 작업을 막고 전체 예측을 수행합니다." }
                    handler.handle(task)
                    log.info { "전체 예측 수행 완료. 쓰기 락을 반납합니다." }
                }
            } else {
                // 개별 티커 작업: 글로벌 읽기 락(Read Lock) + 개별 Mutex를 함께 사용
                log.info { "개별 작업(${tickerId}) 시작. 읽기 락 획득 시도..." }
                coroutineReadWriteLock.read {
                    // 읽기 락(티커별 작업) 안에서 동일 티커 간의 충돌을 막기 위해 개별 뮤텍스를 사용
                    val tickerMutex = mutexes.computeIfAbsent(tickerId) { Mutex() }
                    tickerMutex.withLock {
                        log.info { "개별 락 획득 성공. (${tickerId}): 예측을 수행합니다." }
                        handler.handle(task)
                        log.info { "(${tickerId}): 예측 수행 완료. 개별 락을 반납합니다." }
                    }
                }
            }
        }
    }

    private fun createArticle(article: LambdaArticle): ArticleC {
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

    private fun createArticleInsights(insights: List<ArticleRealTimeInsightRequest>): List<ArticleInsight> {
        return insights.map {
            ArticleInsight(
                it.tickerCode,
                it.sentiment,
                it.reasoning
            )
        }.toList()
    }

}