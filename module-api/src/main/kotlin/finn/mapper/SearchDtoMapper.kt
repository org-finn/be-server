package finn.mapper

import finn.converter.getAbstractDateBefore
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.PredictionQueryDto
import finn.queryDto.TickerQueryDto
import finn.response.article.ArticleListResponse
import finn.response.prediciton.PredictionListResponse
import finn.response.prediciton.PredictionListResponse.PredictionDataResponse
import finn.response.search.ArticleSearchListResponse
import finn.response.search.SearchPreviewResponse
import finn.response.search.TickerSearchListResponse

class SearchDtoMapper {
    companion object {
        fun toSearchPreviewDto(
            tickerDto: List<TickerQueryDto>,
            articleDto: List<ArticleDataQueryDto>
        ): SearchPreviewResponse {
            val tickerList = tickerDto.map {
                SearchPreviewResponse.TickerSearchPreviewResponse(
                    it.tickerId, it.tickerCode, it.shortCompanyName,
                    it.fullCompanyName
                )
            }.toList()

            val articleList = articleDto.map {
                SearchPreviewResponse.ArticleSearchPreviewResponse(
                    it.id,
                    it.title
                )
            }.toList()

            return SearchPreviewResponse(tickerList, articleList)
        }

        fun toSearchListDto(
            tickerDto: List<PredictionQueryDto>
        ): TickerSearchListResponse {
            val tickerList = tickerDto.map {
                PredictionDataResponse(
                    it.tickerId,
                    it.shortCompanyName,
                    it.tickerCode,
                    it.predictionStrategy,
                    it.sentiment,
                    it.articleCount,
                    it.positiveKeywords,
                    it.negativeKeywords,
                    it.isFavorite,
                    it.articleTitles?.map {
                        PredictionListResponse.ArticleTitleResponse(it.articleId, it.title)
                    },
                    it.graphData?.let {
                        PredictionListResponse.PredictionListGraphDataResponse(
                            it.marketOpen,
                            it.priceData
                        )
                    }
                )
            }.toList().take(3)
            return TickerSearchListResponse(tickerList, tickerDto.size > 3)
        }

        fun toSearchListDto(articleDto: List<ArticleDataQueryDto>): ArticleSearchListResponse {
            val articleList = articleDto.map {
                ArticleListResponse.ArticleDataResponse(
                    it.id,
                    it.title,
                    it.description,
                    it.tickers,
                    it.thumbnailUrl,
                    it.contentUrl,
                    getAbstractDateBefore(it.publishedDate),
                    it.source, it.isFavorite
                )
            }.toList().take(3)
            return ArticleSearchListResponse(articleList, articleDto.size > 3)
        }
    }
}
