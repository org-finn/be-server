package finn.orchestrator

import finn.handler.PredictionHandlerFactory
import finn.mapper.toDto
import finn.paging.PredictionPageRequest
import finn.response.prediciton.PredictionDetailResponse
import finn.response.prediciton.PredictionListResponse
import finn.service.ArticleQueryService
import finn.service.PredictionQueryService
import finn.task.PredictionTask
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class PredictionOrchestrator(
    private val handlerFactory: PredictionHandlerFactory,
    private val predictionQueryService: PredictionQueryService,
    private val articleQueryService: ArticleQueryService
) {
    companion object {
        private val log = KotlinLogging.logger {}
        private val wildcard: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }

    @ExposedTransactional
    suspend fun processBatch(tasks: List<PredictionTask>) {
        // 1. 와일드카드(전체 계산) 작업 분리
        val wildcardTasks = tasks.filter { it.tickerId == wildcard }
        val specificTasks = tasks.filter { it.tickerId != wildcard }

        // 2. 일반 종목 배치 처리 (타입별로 핸들러 찾아서 위임)
        if (specificTasks.isNotEmpty()) {
            val tasksByType = specificTasks.groupBy { it.type }

            tasksByType.forEach { (type, tasks) ->
                val handler = handlerFactory.findHandler(type)
                log.info { "Processing batch for type: $type, count: ${tasks.size}" }
                handler.handle(tasks)
            }
        }

        // 3. 와일드카드(글로벌) 작업 처리
        wildcardTasks.forEach { task ->
            log.info { "Processing Wildcard Task" }
            handlerFactory.findHandler(task.type).handle(listOf(task))
        }
    }

    fun getRecentPredictionList(
        pageRequest: PredictionPageRequest,
        userId: UUID?
    ): PredictionListResponse {
        val predictionList = predictionQueryService.getPredictionList(pageRequest, userId)
        return toDto(predictionList)
    }

    fun getPredictionDetail(tickerId: UUID): PredictionDetailResponse {
        val predictionDetail = predictionQueryService.getPredictionDetail(tickerId)
        val articleList = articleQueryService.getArticleDataForPredictionDetail(tickerId)
        return toDto(predictionDetail, articleList)
    }
}