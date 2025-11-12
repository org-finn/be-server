package finn.converter

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun getAbstractDateBefore(contentDate: LocalDateTime): String {
    val today = LocalDateTime.now() // 시간 계산은 UTC끼리 비교해야함
    println("날짜 정보: $contentDate, $today")
    if (contentDate.isAfter(today)) {
        return "방금 전"
    }

    val diffYears = ChronoUnit.YEARS.between(contentDate, today)
    if (diffYears > 0) {
        return diffYears.toString() + "년 전"
    }

    val diffMonths = ChronoUnit.MONTHS.between(contentDate, today)
    if (diffMonths > 0) {
        return diffMonths.toString() + "달 전"
    }

    val diffDays = ChronoUnit.DAYS.between(contentDate, today)
    if (diffDays > 0) {
        if (diffDays == 1L) {
            return "하루 전"
        }
        return diffDays.toString() + "일 전"
    }

    val diffHours = ChronoUnit.HOURS.between(contentDate, today)
    if (diffHours > 0) {
        return diffHours.toString() + "시간 전"
    }

    val diffMinutes = ChronoUnit.MINUTES.between(contentDate, today)
    if (diffMinutes > 0) {
        return diffMinutes.toString() + "분 전"
    }

    return "방금 전"
}
