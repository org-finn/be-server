package finn.mapper

import finn.paging.PageResponse
import finn.queryDto.TickerJoinQueryDto
import finn.queryDto.TickerQueryDto
import finn.response.article.ArticleTickerFilteringListResponse
import finn.response.article.ArticleTickerFilteringListResponse.ArticleTickerFilteringResponse
import finn.response.userinfo.JoinTickerResponse

class TickerDtoMapper {
    companion object {
        fun toDto(tickerList: List<TickerQueryDto>): ArticleTickerFilteringListResponse {
            return ArticleTickerFilteringListResponse(tickerList.map {
                ArticleTickerFilteringResponse(it.tickerId, it.shortCompanyName, it.tickerCode)
            }.toList())
        }

        fun toDto(tickerList: PageResponse<TickerJoinQueryDto>): JoinTickerResponse {
            return JoinTickerResponse(
                tickerList.content.map {
                    JoinTickerResponse.JoinTicker(
                        it.tickerCode,
                        it.shortCompanyName,
                        it.predictionStrategy
                    )
                }.toList(),
                tickerList.page,
                tickerList.hasNext
            )
        }
    }
}
