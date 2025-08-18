package finn.moduleDomain.converter

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object BusinessDayLocalizer {
    fun getTradingHours(): String {
        val usEasternZone = ZoneId.of("America/New_York")
        val koreaZone = ZoneId.of("Asia/Seoul")

        // 미국 증시 개장 및 폐장 시간 정의
        val marketOpenTime = LocalTime.of(9, 30)
        val marketCloseTime = LocalTime.of(16, 0)

        // 현재 날짜를 기준으로 미국 동부 시간의 ZonedDateTime 객체 생성
        // 이 과정에서 ZonedDateTime이 자동으로 서머타임 여부를 판단하여 UTC 오프셋을 결정
        val usMarketOpen = ZonedDateTime.of(
            LocalDate.now(usEasternZone), marketOpenTime,
            usEasternZone
        )
        val usMarketClose = ZonedDateTime.of(
            LocalDate.now(usEasternZone),
            marketCloseTime, usEasternZone
        )

        // 미국 시간을 한국 시간으로 변환
        val kstMarketOpen = usMarketOpen.withZoneSameInstant(koreaZone)
        val kstMarketClose = usMarketClose.withZoneSameInstant(koreaZone)

        // 원하는 형식("HH:mm")으로 포맷팅
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        return String.format(
            "%s~%s", kstMarketOpen.format(formatter),
            kstMarketClose.format(formatter)
        )
    }
}