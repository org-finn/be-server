package finn.filter

import finn.exception.DomainPolicyViolationException
import finn.queryDto.TickerQueryDto
import org.springframework.stereotype.Service

@Service
class TickerSearchFilter {

    fun filterByKeyword(
        tickers: List<TickerQueryDto>,
        keyword: String
    ): List<TickerQueryDto> {
        if (keyword.isBlank() || keyword.length < 2) {
            throw DomainPolicyViolationException("필터링 조건 키워드는 2글자 이상만 가능합니다.")
        }
        return tickers.filter {
            it.shortCompanyName().startsWith(keyword, ignoreCase = true) ||
                    it.shortCompanyNameKr().startsWith(keyword, ignoreCase = true)
        }
    }
}