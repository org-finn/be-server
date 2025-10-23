package finn.strategy

import finn.task.InitPredictionTask
import org.springframework.stereotype.Component
import kotlin.math.abs
import kotlin.math.max

@Component
class ATRExponentStrategy : TechnicalExponentStrategy<InitPredictionTask> {
    override fun supports(type: String): Boolean = type == "init"

    /**
     * 오늘의 TR(True Range)을 계산하고,
     * 이를 NATR(Normalized ATR)로 변환하여 반환합니다.
     *
     * @return NATR 값을 정수(Int)로 스케일링한 값 (예: 5.25% -> 525)
     */

    // ATR 기간 (표준: 14)
    private val period: Int = 14

    override suspend fun calculate(task: InitPredictionTask): Pair<Double, Double> {
        val payload = task.payload

        // 1. 오늘 하루의 'True Range' (TR)을 계산합니다.
        val todayTr = calculateTrueRange(
            todayHigh = payload.todayHigh,
            todayLow = payload.todayLow,
            yesterdayClose = payload.yesterdayClose
        )

        // 2. 어제의 ATR 값을 가져옵니다.
        val yesterdayAtr = payload.yesterdayAtr

        // 3. 스무딩(Smoothing) 공식을 사용해 '오늘의 ATR'을 계산합니다.
        val todayAtr = ((yesterdayAtr * (period - 1)) + todayTr) / period

        // 4. 오늘의 ATR을 'Normalized ATR' (NATR)로 변환합니다.
        val natr = calculateNATR(
            trueRange = todayAtr,
            previousClose = payload.yesterdayClose
        )

        // 5. NATR(비율)을 정수로 변환하여 반환합니다.
        //    (예: 0.0525 -> 5.25% -> 525)
        return Pair(natr, todayAtr)
    }

    /**
     * 1단계: TR (True Range) 계산
     * 그날의 실제 변동폭을 갭(Gap)까지 포함하여 계산합니다.
     */
    private fun calculateTrueRange(
        todayHigh: Double,
        todayLow: Double,
        yesterdayClose: Double
    ): Double {
        val tr1 = todayHigh - todayLow
        val tr2 = abs(todayHigh - yesterdayClose)
        val tr3 = abs(todayLow - yesterdayClose)

        return max(tr1, max(tr2, tr3))
    }

    /**
     * 2단계: NATR (Normalized ATR) 계산
     * TR 값을 이전 종가로 나누어 가격과 무관하게 비교 가능한 비율(%)로 만듭니다.
     */
    private fun calculateNATR(trueRange: Double, previousClose: Double): Double {
        if (previousClose == 0.0) {
            return 0.0
        }
        // 예: (TR 1,000 / 종가 50,000) = 0.02 (즉, 2%)
        return trueRange / previousClose
    }
}