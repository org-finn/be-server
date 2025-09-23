package finn.strategy

import finn.task.ExponentPredictionTask
import finn.task.ExponentPredictionTask.ExponentListPayload.ExponentPayload
import org.springframework.stereotype.Component

/**
 * ExponentPredictionTask를 받아 시장 전체 상황에 대한 보정 점수를 계산합니다.
 * @param task NDX, VXX 등의 지수 정보가 포함된 작업 객체
 * @return 모든 종목에 공통으로 적용될 시장 보정 점수 (예: -5 ~ +5)
 */
@Component
class ExponentSentimentScoreStrategy : SentimentScoreStrategy<ExponentPredictionTask> {

    override fun supports(type: String): Boolean = type == "exponent"

    override suspend fun calculate(task: ExponentPredictionTask): Int {
        val exponents = task.payload.exponents

        // 1. 리스트에서 나스닥(NDX)과 변동성(VXX) 지수 데이터를 찾습니다.
        // 실제 코드에서는 'name' 필드나 미리 정의된 UUID를 사용해 구분해야 합니다.
        val nasdaqExponent = exponents.find { it.code.equals("NDX", ignoreCase = true) }
        val vixExponent = exponents.find { it.code.equals("VXX", ignoreCase = true) }

        // 2. 각 지수의 변화율(%)을 계산합니다.
        val nasdaqChangePercent = calculateChangePercent(nasdaqExponent)
        val vixChangePercent = calculateChangePercent(vixExponent)

        var adjustmentScore = 0

        // 3. 나스닥 선물 변화율에 따른 점수를 계산합니다.
        adjustmentScore += when {
            nasdaqChangePercent > 0.3 -> 3
            nasdaqChangePercent > 0.1 -> 1
            nasdaqChangePercent < -0.3 -> -3
            nasdaqChangePercent < -0.1 -> -1
            else -> 0
        }

        // 4. VIX(변동성) 변화율에 따른 점수를 계산합니다.
        adjustmentScore += when {
            vixChangePercent < -2.0 -> 2 // VIX 하락은 긍정 신호
            vixChangePercent > 2.0 -> -2 // VIX 상승은 부정 신호
            else -> 0
        }

        return adjustmentScore
    }

    /**
     * 현재 값과 이전 값으로 변화율(%)을 계산하는 헬퍼 함수
     */
    private fun calculateChangePercent(payload: ExponentPayload?): Double {
        if (payload == null || payload.previousValue == 0.0) {
            return 0.0
        }
        return ((payload.value - payload.previousValue) / payload.previousValue) * 100
    }
}