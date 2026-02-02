package finn.entity.query

import finn.converter.BusinessDayLocalizer.Companion.getTradingHours
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

        /**
         * MarketStatus(Nullable)를 기반으로 유효한 TradingHours 문자열을 반환하는 메서드
         * - null이면 기본 정규장 시간 반환
         * - 휴장이면 휴장 문자열 반환
         * - 데이터가 있으면 해당 시간 반환
         */
        fun resolveTradingHours(marketStatus: MarketStatus?): String {
            return marketStatus?.// DB에 설정된 값이 있으면 그 값을 사용 (휴장 or 조기마감 등)
            tradingHours ?: // 데이터가 없으면 평범한 정규장으로 간주
            getTradingHours()
        }

        fun checkIsOpened(marketStatus: MarketStatus?, clock: Clock): Boolean {
            // 0. 주말 선제 검토
            if (isWeekend(LocalDate.now(clock))) return false

            // 1. 문자열 결정 (리팩토링된 메서드 사용)
            val targetTradingHoursKST = resolveTradingHours(marketStatus)

            if (targetTradingHoursKST == getClosedDayTradingHours()) return false

            // --- TradingHours 파싱 및 유효성 검증 ---

            // tradingHours 문자열 (예: "09:00~14:00")에서 시각 정보 파싱
            val hoursStringKST = targetTradingHoursKST.split("~")
            if (hoursStringKST.size != 2) {
                throw DomainPolicyViolationException("유효하지 않은 TradingHours 형식입니다. DB를 확인해주세요. (Value: $targetTradingHoursKST)")
            }

            val formatter = DateTimeFormatter.ofPattern("HH:mm")

            val openTimeKst: LocalTime
            val closeTimeKst: LocalTime

            try {
                openTimeKst = LocalTime.parse(hoursStringKST[0].trim(), formatter)
                closeTimeKst = LocalTime.parse(hoursStringKST[1].trim(), formatter)
            } catch (e: Exception) {
                throw DomainPolicyViolationException("유효하지 않은 TradingHours 형식입니다. DB를 확인해주세요.")
            }

            // --- KST -> UTC 변환 및 검증 로직 ---

            val kstZone = ZoneId.of("Asia/Seoul")

            // clock은 이미 UTC 기준이라고 가정하지만, 안전을 위해 clock.zone을 사용하거나 명시적으로 UTC 변환
            // 여기서는 clock의 타임존을 그대로 따르도록 clock.zone을 사용합니다.
            // 만약 clock이 UTC라면 utcZone은 ZoneId.of("UTC")가 됩니다.
            val currentZone = clock.zone

            // KST 기준의 오늘 날짜를 구함 (개장 시간 기준일을 맞추기 위함)
            // 주의: 현재 시각(UTC)을 KST로 변환했을 때의 날짜를 기준으로 해야 함
            val todayKst = LocalDate.now(clock.withZone(kstZone))

            // KST ZonedDateTime 생성 (오늘 날짜 + 파싱된 KST 시간)
            val openZdtKst = ZonedDateTime.of(todayKst, openTimeKst, kstZone)
            val closeZdtKst = ZonedDateTime.of(todayKst, closeTimeKst, kstZone)

            // 현재 clock의 타임존(UTC)으로 변환된 LocalTime 추출
            val openTimeCurrentZone = openZdtKst.withZoneSameInstant(currentZone).toLocalTime()
            val closeTimeCurrentZone = closeZdtKst.withZoneSameInstant(currentZone).toLocalTime()

            // 현재 시각 (clock 기준)
            val curTime = LocalTime.now(clock)

            // 오픈 시각 <= 현재 시각 < 클로즈드 시각인지 체크
            // 날짜가 넘어가는 경우(예: UTC로 변환했더니 23:00 ~ 05:00이 된 경우)를 처리하기 위해 로직 분기
            return if (openTimeCurrentZone.isBefore(closeTimeCurrentZone)) {
                // 일반적인 경우 (예: 00:00 ~ 06:30) -> AND 조건
                !curTime.isBefore(openTimeCurrentZone) && curTime.isBefore(closeTimeCurrentZone)
            } else {
                // 자정을 넘어가는 경우 (예: 22:30 ~ 05:00) -> OR 조건
                !curTime.isBefore(openTimeCurrentZone) || curTime.isBefore(closeTimeCurrentZone)
            }
        }

        fun calculateMaxLen(tradingHours: String): Int {
            // 1. 휴장인 경우 길이는 0
            if (tradingHours == getClosedDayTradingHours()) {
                return 0
            }

            // 2. 파싱 (예: "22:30~05:00")
            val parts = tradingHours.split("~")
            if (parts.size != 2) {
                throw DomainPolicyViolationException("유효하지 않은 TradingHours 형식입니다. (Value: $tradingHours)")
            }

            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val startTime: LocalTime
            val endTime: LocalTime

            try {
                startTime = LocalTime.parse(parts[0].trim(), formatter)
                endTime = LocalTime.parse(parts[1].trim(), formatter)
            } catch (e: Exception) {
                throw DomainPolicyViolationException("TradingHours 시간 파싱 중 오류가 발생했습니다. (Value: $tradingHours)")
            }

            // 3. 시간 차이 계산
            // Duration.between(start, end)는 start < end 이면 양수, start > end 이면 음수를 반환
            var duration = Duration.between(startTime, endTime)

            // 자정을 넘가는 경우 (예: 22:30 시작, 05:00 종료) duration은 음수가 됨 (예: -17시간 30분)
            // 이 경우 하루(24시간)를 더해주면 실제 운영 시간이 됨
            if (duration.isNegative) {
                duration = duration.plusDays(1)
            }

            return duration.toMinutes().toInt()
        }
    }

    fun checkIsClosedDay(): Boolean {
        return tradingHours == getClosedDayTradingHours()
    }
}