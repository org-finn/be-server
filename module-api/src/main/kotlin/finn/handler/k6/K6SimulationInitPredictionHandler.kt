package finn.handler.k6

import finn.exception.NotSupportedTypeException
import finn.policy.isPreviousDayHoliday
import finn.service.k6.K6SimulationPredictionService
import finn.strategy.ATRExponentStrategy
import finn.strategy.PredictionInitSentimentScoreStrategy
import finn.task.InitPredictionTask
import finn.task.PredictionTask
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class K6SimulationInitPredictionHandler(
    private val predictionService: K6SimulationPredictionService,
    // 실제 전략 로직을 주입받아 사용 (CPU 연산 수행)
    private val sentimentScoreStrategy: PredictionInitSentimentScoreStrategy,
    private val technicalExponentStrategy: ATRExponentStrategy
) : K6SimulationPredictionHandler {

    override fun supports(type: String): Boolean = type == "init"

    override fun handleBlocking(task: PredictionTask) {
        if (task !is InitPredictionTask) {
            throw NotSupportedTypeException("Unsupported prediction task type in Init Prediction: ${task.type}")
        }
        // [I/O] 1. 조회 (Blocking)
        predictionService.readBlocking()

        task.payload.recentScores = listOf(50, 60, 70, 55, 45)

        // [CPU] 2. 계산
        // 전략 로직이 suspend 함수라면 runBlocking으로 감싸야 함
        val score = runBlocking { sentimentScoreStrategy.calculate(task) }

        if (isPreviousDayHoliday()) {
            // [I/O] 3-A. 조회
            predictionService.readBlocking()
            // [I/O] 4-A. 저장
            predictionService.writeBlocking()
        } else {
            // [I/O] 3-B. 조회
            predictionService.readBlocking()

            task.payload.yesterdayAtr = BigDecimal("1.5")

            // [CPU] 4-B. 계산
            runBlocking { technicalExponentStrategy.calculate(task) }

            // [I/O] 5-B. 저장
            predictionService.writeBlocking()
            // [I/O] 6-B. 업데이트
            predictionService.writeBlocking()
        }
    }

    override suspend fun handleNonBlocking(task: PredictionTask) {
        if (task !is InitPredictionTask) {
            throw NotSupportedTypeException("Unsupported prediction task type in Init Prediction: ${task.type}")
        }
        // [I/O] 1. 최근 감성 점수 조회 시뮬레이션
        predictionService.readNonBlocking()

        // 더미 데이터 채우기 (전략 계산을 위해 필요)
        task.payload.recentScores = listOf(50, 60, 70, 55, 45)

        // [CPU] 2. 감성 점수 전략 계산 (실제 로직 수행)
        // (타입 캐스팅 생략 혹은 안전하게 처리)
        val score = sentimentScoreStrategy.calculate(task)

        // [Logic] 휴일 체크
        if (isPreviousDayHoliday()) {
            // [I/O] 3-A. 어제 변동성 조회
            predictionService.readNonBlocking()

            // [I/O] 4-A. 예측 결과 저장
            predictionService.writeNonBlocking()
        } else {
            // [I/O] 3-B. 어제 ATR 조회
            predictionService.readNonBlocking()

            task.payload.yesterdayAtr = BigDecimal("1.5") // 더미 값

            // [CPU] 4-B. 기술적 지표(ATR) 전략 계산 (실제 로직 수행)
            technicalExponentStrategy.calculate(task)

            // [I/O] 5-B. 예측 결과 저장
            predictionService.writeNonBlocking()

            // [I/O] 6-B. ATR 업데이트
            predictionService.writeNonBlocking()
        }
    }
}