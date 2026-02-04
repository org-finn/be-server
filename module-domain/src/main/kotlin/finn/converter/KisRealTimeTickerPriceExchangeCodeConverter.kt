package finn.converter

import finn.exception.DomainPolicyViolationException

fun toKisCode(exchangeCode: String, tickerCode: String): String {
    return when (exchangeCode) {
        "NYS" -> "DNYS$tickerCode"
        "NAS" -> "DNAS$tickerCode"
        else -> throw DomainPolicyViolationException("Exchange code not supported: $exchangeCode")
    }
}
