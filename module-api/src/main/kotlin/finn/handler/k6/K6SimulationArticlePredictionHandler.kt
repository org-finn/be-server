package finn.handler.k6

import finn.exception.NotSupportedTypeException
import finn.service.k6.K6SimulationPredictionService
import finn.strategy.ArticleSentimentScoreStrategy
import finn.task.ArticlePredictionTask
import finn.task.PredictionTask
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class K6SimulationArticlePredictionHandler(
    private val predictionService: K6SimulationPredictionService,
    // 실제 전략 로직을 주입받아 사용 (CPU 연산 수행)
    private val sentimentScoreStrategy: ArticleSentimentScoreStrategy
) : K6SimulationPredictionHandler {

    override fun supports(type: String): Boolean = type == "article"

    // === B. Blocking (Thread-based) 흐름 ===
    // 시작부터 끝까지 스레드 하나를 독점합니다.
    override fun handleBlocking(task: PredictionTask) {
        if (task !is ArticlePredictionTask) {
            throw NotSupportedTypeException("Unsupported prediction task type in Article Prediction: ${task.type}")
        }
        // 1. [I/O] 이전 점수 조회 (Blocking)
        // Thread.sleep으로 인해 스레드가 멈춤
        val previousScore = predictionService.readBlocking()

        // 2. [CPU] 점수 계산 (실제 알고리즘 수행)
        // strategy.calculate가 suspend 함수라면 runBlocking으로 감싸야 함
        // (순수 계산 로직이라면 suspend를 떼는 것이 맞으나, 편의상 runBlocking 사용)
        task.payload.previousScore = previousScore.toInt()
        val newScore = runBlocking {
            sentimentScoreStrategy.calculate(task)
        }

        // 3. [I/O] 결과 DB 반영 (Blocking)
        // Thread.sleep으로 인해 스레드가 멈춤
        predictionService.writeBlocking()
    }


    // === A. Coroutine (Non-Blocking) 흐름 ===
    // I/O 구간에서는 스레드를 놔주고, CPU 구간에서만 스레드를 씁니다.
    override suspend fun handleNonBlocking(task: PredictionTask) {
        if (task !is ArticlePredictionTask) {
            throw NotSupportedTypeException("Unsupported prediction task type in Article Prediction: ${task.type}")
        }
        // 1. [I/O] 이전 점수 조회 (Non-blocking)
        val previousScore = predictionService.readNonBlocking()

        // 2. [CPU] 점수 계산 (실제 알고리즘 수행)
        task.payload.previousScore = previousScore.toInt() // 타입 맞춤
        val newScore = sentimentScoreStrategy.calculate(task)

        // 3. [I/O] 결과 DB 반영 (Non-blocking)
        predictionService.writeNonBlocking()
    }
}