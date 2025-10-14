package finn.consumer

import finn.exception.SQSException
import finn.orchestrator.PredictionOrchestrator
import finn.task.PredictionTask
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.SqsHeaders
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class SqsMessageConsumer(
    private val predictionOrchestrator: PredictionOrchestrator
) {

    companion object {
        private val log = KotlinLogging.logger {}
        private const val MAX_RETRY_COUNT = 5
    }

    @SqsListener("\${sqs.queue.name}")
    fun receiveMessage(
        task: PredictionTask,
        @Header(SqsHeaders.MessageSystemAttributes.SQS_APPROXIMATE_RECEIVE_COUNT) receiveCountStr: String // 카운트를 메시지 헤더에서 자동으로 관리
    ) {
        val currentRetryCount = receiveCountStr.toInt()
        log.debug { "Received message from SQS, Type: ${task.type}, TickerId: ${task.tickerId}, ReceiveCount: $currentRetryCount" }

        try {
            predictionOrchestrator.updatePrediction(task)
            log.debug { "Successfully processed for tickerId: ${task.tickerId}" }

        } catch (e: Exception) {
            if (currentRetryCount >= MAX_RETRY_COUNT) { // 재시도 횟수 5회 이상: 메시지 폐기
                log.error { "[FATAL] Max retries ($MAX_RETRY_COUNT) exceeded for tickerId: ${task.tickerId}. Discarding message." }
                throw SQSException("Fatal error after max retries, triggering alarm", e)
            } else { // 재시도 횟수 5회 미만: DLQ로 전송(예외를 던져야 DLQ로 전송됨)
                log.warn {
                    "Transient failure for tickerId: ${task.tickerId} (Retry: ${currentRetryCount}/${MAX_RETRY_COUNT}).This will be moved to DLQ."
                }
                throw SQSException("Transient failure, will be retried or moved to DLQ", e)
            }
        }
    }
}