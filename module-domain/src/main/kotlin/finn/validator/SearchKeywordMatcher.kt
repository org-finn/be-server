package finn.validator

import finn.entity.query.Ticker
import finn.exception.DomainPolicyViolationException
import java.util.*

fun checkTickerMatchCondition(ticker: Ticker, keyword: String): Boolean {
    checkKeywordValid(keyword)
    return ticker.shortCompanyName.lowercase(Locale.getDefault())
        .startsWith(keyword.lowercase(Locale.getDefault()))
}

fun checkKeywordValid(keyword: String?) {
    if (keyword.isNullOrBlank() || keyword.length < 2) {
        throw DomainPolicyViolationException("키워드는 2글자 이상만 요청할 수 있습니다.")
    }
}
