package finn.policy

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 금일이 UTC 기준 일/월인지 여부 검사
 */
fun isPreviousDayHoliday(): Boolean {
    return (LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY || LocalDate.now().dayOfWeek == DayOfWeek.MONDAY)
}