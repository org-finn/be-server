package finn.mapper

import finn.queryDto.TickerQueryDto
import finn.response.article.ArticleTickerFilteringListResponse
import finn.response.article.ArticleTickerFilteringListResponse.ArticleTickerFilteringResponse

class TickerDtoMapper {
    companion object {
        fun toDto(tickerList: List<TickerQueryDto>): ArticleTickerFilteringListResponse {
            return ArticleTickerFilteringListResponse(tickerList.map {
                ArticleTickerFilteringResponse(it.tickerId(), it.shortCompanyName())
            }.toList())
        }
    }
}
