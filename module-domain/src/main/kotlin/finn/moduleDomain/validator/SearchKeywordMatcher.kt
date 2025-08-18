package finn.moduleDomain.validator

import finn.moduleDomain.entity.Ticker
import finn.moduleDomain.exception.BadRequestDomainPolicyViolationException
import java.util.*

object SearchKeywordMatcher {
    fun checkTickerMatchCondition(ticker: Ticker, keyword: String): Boolean {
        checkKeywordValid(keyword)
        return ticker.shortCompanyName.lowercase(Locale.getDefault())
            .startsWith(keyword.lowercase(Locale.getDefault()))
    }

    private fun checkKeywordValid(keyword: String) {
        if (keyword.isBlank() || keyword.length < 2) {
            throw BadRequestDomainPolicyViolationException("키워드는 2글자 이상만 요청할 수 있습니다.")
        }
    }
}