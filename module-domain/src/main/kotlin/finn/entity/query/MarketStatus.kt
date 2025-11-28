package finn.entity.query

import finn.converter.getTradingHours
import finn.exception.DomainPolicyViolationException
import java.time.*
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

        fun checkIsOpened(marketStatus: MarketStatus?, clock: Clock): Boolean {
            if (marketStatus == null) {
                return false
            }

            // tradingHours 문자열 (예: "09:00~14:00")에서 시각 정보 파싱
            val hoursString = marketStatus.tradingHours.split("~")
            if (hoursString.size != 2) {
                // 형식이 올바르지 않은 경우 (예: 예외 처리)
                throw DomainPolicyViolationException("유효하지 않은 TradingHours 형식입니다. DB를 확인해주세요.")
            }

            val formatter = DateTimeFormatter.ofPattern("HH:mm")

            val openTime: LocalTime
            val closeTime: LocalTime

            try {
                // LocalTime 객체로 변환
                openTime = LocalTime.parse(hoursString[0].trim(), formatter)
                closeTime = LocalTime.parse(hoursString[1].trim(), formatter)
            } catch (e: Exception) {
                throw DomainPolicyViolationException("유효하지 않은 TradingHours 형식입니다. DB를 확인해주세요.")
            }

            // 현재 시각의 시간(Hour)과 분(Minute) 정보만 추출
            val curTime = LocalDateTime.now(clock).toLocalTime()

            // 오픈 시각 <= 현재 시각 <= 클로즈드 시각인지 체크
            return if (openTime.isBefore(closeTime)) {
                // 일반적인 경우 (예: 09:00~14:00)
                (curTime.equals(openTime) || curTime.isAfter(openTime)) && (curTime.equals(openTime) || curTime.isBefore(
                    closeTime
                ))
            } else {
                // 자정을 넘어가는 경우 (예: 22:00~03:00)
                (curTime.equals(openTime) || curTime.isAfter(openTime)) || (curTime.equals(openTime) || curTime.isBefore(
                    closeTime
                ))
            }
        }
    }

    fun checkIsClosedDay(): Boolean {
        return tradingHours == getClosedDayTradingHours()
    }
}