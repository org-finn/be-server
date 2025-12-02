package finn.entity.query

import finn.converter.getTradingHours
import finn.exception.DomainPolicyViolationException
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MarketStatus private constructor(
    val date: LocalDate,
    val tradingHours: String,
    val eventName: String? = null
) {
    companion object {
        fun create(date: LocalDate, tradingHours: String, eventName: String?): MarketStatus {
            return MarketStatus(date, tradingHours, eventName)
        }

        fun getWeekendMarketStatus(date: LocalDate): MarketStatus {
            return MarketStatus(date, getClosedDayTradingHours(), "Weekend")
        }

        fun getFullOpenedMarketStatus(
            date: LocalDate,
        ): MarketStatus {
            return MarketStatus(date, getTradingHours(), null)
        }

        fun isWeekend(date: LocalDate): Boolean {
            val dayOfWeek = date.getDayOfWeek()
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
        }

        fun getClosedDayTradingHours(): String {
            return "휴장"
        }

        /**
         * trading_hours가 KST 기준이므로, 현재 시각을 KST 기준으로 생성하여 비교
         */
        fun checkIsOpened(marketStatus: MarketStatus?, clock: Clock): Boolean {
            // 1. 개장 시간 문자열 결정
            val targetTradingHours: String = if (marketStatus == null) {
                // marketStatus가 null인 경우: 풀 개장일로 간주(서머타임 변수가 고려된 한국 시간(KST)의 개장 시간을 가져옴)
                getTradingHours()
            } else if (marketStatus.tradingHours == getClosedDayTradingHours()) { // 명시적으로 휴장일("휴장")인 경우 닫힘
                return false
            } else {
                // DB에서 가져온 MarketStatus의 TradingHours 사용
                marketStatus.tradingHours
            }

            // --- TradingHours 파싱 및 유효성 검증 ---

            // tradingHours 문자열 (예: "09:00~14:00")에서 시각 정보 파싱
            val hoursString = targetTradingHours.split("~")
            if (hoursString.size != 2) {
                throw DomainPolicyViolationException("유효하지 않은 TradingHours 형식입니다. DB를 확인해주세요. (Value: $targetTradingHours)")
            }

            val formatter = DateTimeFormatter.ofPattern("HH:mm")

            val openTime: LocalTime
            val closeTime: LocalTime

            try {
                openTime = LocalTime.parse(hoursString[0].trim(), formatter)
                closeTime = LocalTime.parse(hoursString[1].trim(), formatter)
            } catch (e: Exception) {
                throw DomainPolicyViolationException("유효하지 않은 TradingHours 형식입니다. DB를 확인해주세요.")
            }

            // --- 개장 시간 검증 로직 ---

            // 현재 시각의 시간(Hour)과 분(Minute) 정보만 추출
            // clock을 사용하여 현재 시각을 고정하고 LocalTime으로 변환
            val curTime = LocalTime.now(clock)

            // 오픈 시각 <= 현재 시각 < 클로즈드 시각인지 체크
            return if (openTime.isBefore(closeTime)) {
                // 일반적인 경우 (예: 09:00~14:00)
                // curTime >= openTime && curTime < closeTime
                !curTime.isBefore(openTime) && curTime.isBefore(closeTime)
            } else {
                // 자정을 넘어가는 경우 (예: 22:30~05:00)
                // curTime >= openTime || curTime < closeTime
                !curTime.isBefore(openTime) || curTime.isBefore(closeTime)
            }
        }
    }

    fun checkIsClosedDay(): Boolean {
        return tradingHours == getClosedDayTradingHours()
    }
}