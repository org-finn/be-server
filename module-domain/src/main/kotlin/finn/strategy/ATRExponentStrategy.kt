package finn.strategy

import finn.task.InitPredictionTask
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs
import kotlin.math.max

@Component
class ATRExponentStrategy : TechnicalExponentStrategy<InitPredictionTask> {
    override fun supports(type: String): Boolean = type == "init"

    /**
     * 오늘의 TR(True Range)을 계산하고,
     * 이를 NATR(Normalized ATR)로 변환하여 반환합니다.
     */

    // ATR 기간 (표준: 14)
    private val period: Int = 14

    // 나눗셈 연산을 위한 정밀도 설정 (Double과 유사한 정밀도)
    private val mc = MathContext.DECIMAL64

    override suspend fun calculate(task: InitPredictionTask): Pair<Double, Double> {
        val payload = task.payload

        // 1. 오늘 하루의 'True Range' (TR)을 Double로 계산합니다.
        val todayTrDouble = calculateTrueRange(
            todayHigh = payload.todayHigh,
            todayLow = payload.todayLow,
            yesterdayClose = payload.yesterdayClose
        )

        // --- 2. 모든 연산 값을 BigDecimal로 변환합니다. ---
        val todayTrBd = todayTrDouble.toBigDecimal()
        val yesterdayAtrBd = payload.yesterdayAtr // 이미 BigDecimal
        val periodBd = period.toBigDecimal()

        // 3. 스무딩 공식을 'BigDecimal' 연산으로 안전하게 계산합니다.
        // 공식: ((yesterdayAtr * (period - 1)) + todayTr) / period
        val periodMinusOne = periodBd.subtract(BigDecimal.ONE)

        // 분자 계산: (yesterdayAtr * (period - 1)) + todayTr
        val numerator = yesterdayAtrBd.multiply(periodMinusOne).add(todayTrBd)

        // 최종 ATR 계산: 분자 / period (안전한 나눗셈을 위해 MathContext 사용)
        val todayAtrBd = numerator.divide(periodBd, mc)

        // 4. 오늘의 ATR을 'Normalized ATR' (NATR)로 변환합니다.
        val yesterdayCloseBd = payload.yesterdayClose.toBigDecimal()
        val natrBd = calculateNATR(
            trueRange = todayAtrBd,
            previousClose = yesterdayCloseBd
        )

        // 5. 최종 결과를 Double로 변환하여 반환합니다.
        return Pair(natrBd.toDouble(), todayAtrBd.toDouble())
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
    private fun calculateNATR(trueRange: BigDecimal, previousClose: BigDecimal): BigDecimal {
        // 0으로 나누는 오류 방지
        if (previousClose == BigDecimal.ZERO) {
            return BigDecimal.ZERO
        }
        // MathContext를 사용해 안전하게 나눗셈
        return trueRange.divide(previousClose, mc)
    }
}