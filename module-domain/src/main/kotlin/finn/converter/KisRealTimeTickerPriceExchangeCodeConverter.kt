package finn.converter

import finn.exception.DomainPolicyViolationException

fun toKisCode(exchangeCode: String, tickerCode: String): String {
    return when (exchangeCode) {
        "NYSE" -> "DNYS$tickerCode"
        "NASD" -> "DNAS$tickerCode"
        else -> throw DomainPolicyViolationException("Exchange code not supported: $exchangeCode")
    }
}

fun toDomainCode(kisCode: String): String {
    return kisCode.substring(4) // D + {NYS, NAS} 총 4문자를 절삭
}