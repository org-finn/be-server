package finn.mapper

import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.TickerQueryDto
import finn.response.search.TickerSearchPreviewListResponse
import finn.response.search.ArticleSearchListResponse

class SearchDtoMapper {
    companion object {
        fun toDto(tickerDto: List<TickerQueryDto>): TickerSearchPreviewListResponse {
            val tickerList = tickerDto.map {
                TickerSearchPreviewListResponse.TickerSearchPreviewResponse(
                    it.tickerId, it.tickerCode, it.shortCompanyName,
                    it.fullCompanyName
                )
            }.toList()
            return TickerSearchPreviewListResponse(tickerList)
        }

        fun toDto(tickerDto: List<TickerQueryDto>, isKorean: Boolean): TickerSearchPreviewListResponse {
            val tickerList = tickerDto.map {
                TickerSearchPreviewListResponse.TickerSearchPreviewResponse(
                    it.tickerId, 
                    it.tickerCode, 
                    if (isKorean) it.shortCompanyNameKr else it.shortCompanyName,
                    it.fullCompanyName
                )
            }.toList()
            return TickerSearchPreviewListResponse(tickerList)
        }
        
        fun toArticleSearchDto(articleDto: List<ArticleDataQueryDto>): ArticleSearchListResponse {
            val articleList = articleDto.map {
                ArticleSearchListResponse.ArticleSearchResponse(
                    it.id,
                    it.title
                )
            }.toList()
            return ArticleSearchListResponse(articleList)
        }
    }
}
