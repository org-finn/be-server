package finn.converter

import finn.exception.DomainPolicyViolationException
import java.time.LocalDate

fun getInterval(period: String): Int {
    return when (period) {
        "2W", "1M" -> 1
        "6M" -> 3
        "1Y" -> 7
        else -> throw DomainPolicyViolationException("지원하지 않는 주기입니다.")
    }
}

fun getStartDate(period: String, endDate: LocalDate): LocalDate {
    return when (period) {
        "2W" -> endDate.minusWeeks(2)
        "1M" -> endDate.minusMonths(1)
        "6M" -> endDate.minusMonths(6)
        "1Y" -> endDate.minusYears(1)
        else -> throw DomainPolicyViolationException("지원하지 않는 주기입니다.")
    }
}

fun getMinimumCount(period: String): Long {
    return when (period) {
        "2W", "1M" -> 1L
        "6M" -> 10L
        "1Y" -> 20L
        else -> throw DomainPolicyViolationException("지원하지 않는 주기입니다.")
    }
}

fun isCachingPeriod(period: String): Boolean {
    return when (period) {
        "2W" -> true
        "1M", "6M", "1Y" -> false
        else -> throw DomainPolicyViolationException("지원하지 않는 주기입니다.")
    }
}