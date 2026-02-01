package finn.converter

import finn.exception.DomainPolicyViolationException

fun convertCode(exchangeCode: String, tickerCode: String): String {
    return when (exchangeCode) {
        "NYSE" -> "DNYS$tickerCode"
        "NASD" -> "DNAS$tickerCode"
        else -> throw DomainPolicyViolationException("Exchange code not supported: $exchangeCode")
    }
}