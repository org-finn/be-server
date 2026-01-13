package finn.consumer

import finn.exception.SQSException
import finn.orchestrator.PredictionOrchestrator
import finn.task.PredictionTask
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.SqsHeaders
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class SqsMessageConsumer(
    private val predictionOrchestrator: PredictionOrchestrator
) {

    companion object {
        private val log = KotlinLogging.logger {}
        private const val MAX_RETRY_COUNT = 5
    }

    @SqsListener(
        queueNames = ["\${sqs.queue.name}"],
        maxMessagesPerPoll = "50", // 배치 사이즈 설정
        maxConcurrentMessages = "50"
    )
    fun receiveBatch(messages: List<Message<PredictionTask>>) {
        if (messages.isEmpty()) return

        val validTasks = mutableListOf<PredictionTask>()

        // 1. 메시지 필터링 및 재시도 검증
        messages.forEach { message ->
            val task = message.payload
            val receiveCount =
                message.headers[SqsHeaders.MessageSystemAttributes.SQS_APPROXIMATE_RECEIVE_COUNT]
                    ?.toString()?.toIntOrNull() ?: 1

            if (receiveCount >= MAX_RETRY_COUNT) {
                log.error { "[FATAL] Max retries ($MAX_RETRY_COUNT) exceeded for tickerId: ${task.tickerId}. Discarding message." }
            } else {
                validTasks.add(task)
            }
        }

        if (validTasks.isEmpty()) return

        try {
            runBlocking {
                predictionOrchestrator.processBatch(validTasks)
            }
            log.info { "Successfully processed batch of ${validTasks.size} tasks." }
        } catch (e: Exception) {
            log.error(e) { "Batch processing failed. Messages will be returned to queue." }
            throw SQSException("Batch processing failed", e)
        }
    }
}