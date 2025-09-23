package finn.strategy

import finn.exception.CriticalDataOmittedException
import finn.task.InitPredictionTask
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class PredictionInitSentimentScoreStrategy : SentimentScoreStrategy<InitPredictionTask> {

    override fun supports(type: String): Boolean = type == "init"

    /**
     * PredictionTask의 payload에 담긴 데이터를 기반으로 초기 점수를 계산합니다.
     */
    override suspend fun calculate(task: InitPredictionTask): Int {
        val recentScores = task.payload.recentScores

        val todayMacd = task.payload.todayMacd
        val yesterdayMacd = task.payload.yesterdayMacd
        val todayMa = task.payload.todayMa
        val yesterdayMa = task.payload.yesterdayMa
        val todayRsi = task.payload.todayRsi

        // --- 점수 계산 로직 수행 ---

        // 초기 점수 설정 (어제 감정 점수)
        val yesterdayScore = getYesterdayScore(recentScores)

        // 기술적 지표별 가/감점 계산
        val macdScore = calculateMacdScore(todayMacd, yesterdayMacd)
        val maScore = calculateMaScore(todayMa, yesterdayMa)
        val rsiScore = calculateRsiScore(todayRsi)

        // 모든 점수를 합산하여 오늘의 기본 점수 생성
        val todayBaseScore = yesterdayScore + macdScore + maScore + rsiScore

        // EMA 공식을 적용하여 최종 점수 계산 (추세 반영)
        return applyEma(todayBaseScore, recentScores)
    }

    private fun getYesterdayScore(scores: List<Int>): Int {
        return scores.lastOrNull() ?: 50
    }

    /**
    MACD 기반 점수 계산
     */
    private fun calculateMacdScore(
        today: Map<String, Double>,
        yesterday: Map<String, Double>
    ): Int {
        val todayMacd =
            today["macd"] ?: throw CriticalDataOmittedException("today macd 데이터가 누락되었습니다.")
        val todaySignal =
            today["signal"] ?: throw CriticalDataOmittedException("today signal 데이터가 누락되었습니다.")
        val yesterdayMacd =
            yesterday["macd"] ?: throw CriticalDataOmittedException("yesterday macd 데이터가 누락되었습니다.")
        val yesterdaySignal = yesterday["signal"]
            ?: throw CriticalDataOmittedException("yesterday signal 데이터가 누락되었습니다.")

        return when {
            todayMacd > todaySignal && yesterdayMacd <= yesterdaySignal -> 5 // 골든크로스
            todayMacd < todaySignal && yesterdayMacd >= yesterdaySignal -> -5 // 데드크로스
            todayMacd > todaySignal -> 2 // 강세
            todayMacd < todaySignal -> -2 // 약세
            else -> 0
        }
    }

    /**
    이동평균선 기반 점수 계산
     */
    private fun calculateMaScore(today: Map<String, Double>, yesterday: Map<String, Double>): Int {
        val todayMa5 = today["ma5"] ?: throw CriticalDataOmittedException("today ma5 데이터가 누락되었습니다.")
        val todayMa20 =
            today["ma20"] ?: throw CriticalDataOmittedException("today ma20 데이터가 누락되었습니다.")
        val yesterdayMa5 =
            yesterday["ma5"] ?: throw CriticalDataOmittedException("yesterday ma5 데이터가 누락되었습니다.")
        val yesterdayMa20 =
            yesterday["ma20"] ?: throw CriticalDataOmittedException("yesterday ma20 데이터가 누락되었습니다.")

        return when {
            todayMa5 > todayMa20 && yesterdayMa5 <= yesterdayMa20 -> 5 // 골든크로스
            todayMa5 < todayMa20 && yesterdayMa5 >= yesterdayMa20 -> -5 // 데드크로스
            todayMa5 > todayMa20 -> 2 // 정배열
            todayMa5 < todayMa20 -> -2 // 역배열
            else -> 0
        }
    }

    /**
    RSI 기반 점수 계산
     */
    private fun calculateRsiScore(rsi: Double): Int {
        return when {
            rsi < 30 -> 4   // 과매도
            rsi > 70 -> -4  // 과매수
            else -> 0       // 중립
        }
    }

    /**
    EMA 공식을 적용한 최근 7일 점수 모멘텀 계산
     */
    private fun applyEma(todayScore: Int, recentScores: List<Int>, period: Int = 7): Int {
        val allScores = recentScores + todayScore

        // 기간을 채울 데이터가 부족하면, 단순 평균을 반환
        if (allScores.size < period) {
            return allScores.average().roundToInt().coerceIn(0, 100)
        }

        val alpha = 2.0 / (period + 1)

        // 1. 첫 'period' 개 데이터의 단순 이동 평균(SMA)을 사용하여 초기 EMA를 계산
        var ema = allScores.take(period).average()

        // 2. 나머지 데이터가 있다면, EMA를 반복적으로 계산
        for (i in period until allScores.size) {
            val currentScore = allScores[i].toDouble()
            ema = (currentScore * alpha) + (ema * (1 - alpha))
        }

        return ema.roundToInt().coerceIn(0, 100)
    }
}