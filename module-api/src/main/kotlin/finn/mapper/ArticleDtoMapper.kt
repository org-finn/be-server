package finn.mapper

import finn.converter.getAbstractDateBefore
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.ArticleDetailQueryDto
import finn.response.article.ArticleDetailResponse
import finn.response.article.ArticleListResponse
import finn.response.article.ArticleSearchListResponse
import java.time.format.DateTimeFormatter

class ArticleDtoMapper {
    companion object {
        fun toDto(articleData: PageResponse<ArticleDataQueryDto>): ArticleListResponse {
            val ArticleList = articleData.content.map {
                ArticleListResponse.ArticleDataResponse(
                    it.id, it.title, it.description,
                    it.tickers, it.thumbnailUrl, it.contentUrl,
                    getAbstractDateBefore(it.publishedDate), it.source, it.isFavorite
                )
            }.toList()
            return ArticleListResponse(ArticleList, articleData.page, articleData.hasNext)
        }

        fun toDto(articleDetailData: ArticleDetailQueryDto): ArticleDetailResponse {
            val tickers = articleDetailData.tickers?.map {
                ArticleDetailResponse.ArticleDetailTickerResponse(
                    it.shortCompanyName,
                    it.sentiment,
                    it.reasoning
                )
            }?.toList()
            return ArticleDetailResponse(
                articleDetailData.articleId,
                articleDetailData.headline,
                articleDetailData.description,
                articleDetailData.thumbnailUrl,
                articleDetailData.contentUrl,
                articleDetailData.publishedDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .replace('T', ' '),
                articleDetailData.source,
                tickers,
                articleDetailData.isFavorite
            )
        }

        fun toDto(articleSearchData: List<ArticleDataQueryDto>): ArticleSearchListResponse {
            return ArticleSearchListResponse(
                articleSearchData.map {
                    ArticleListResponse.ArticleDataResponse(
                        it.id, it.title, it.description,
                        it.tickers, it.thumbnailUrl, it.contentUrl,
                        getAbstractDateBefore(it.publishedDate), it.source, it.isFavorite
                    )
                }
            )
        }
    }
}
