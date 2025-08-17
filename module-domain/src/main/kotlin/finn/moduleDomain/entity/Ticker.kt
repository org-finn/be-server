package finn.moduleDomain.entity

import finn.moduleDomain.exception.BadRequestDomainPolicyViolationException
import java.util.*

data class Ticker(
    val id: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val fullCompanyName: String
) {
    fun checkKeywordValid(keyword: String) {
        if (keyword.isBlank() || keyword.length < 2) {
            throw BadRequestDomainPolicyViolationException("키워드는 2글자 이상만 요청할 수 있습니다.")
        }
    }

    fun isMatchInSearchCondition(keyword: String): Boolean {
        checkKeywordValid(keyword)
        return shortCompanyName.lowercase(Locale.getDefault())
            .startsWith(keyword.lowercase(Locale.getDefault()))
    }
}
