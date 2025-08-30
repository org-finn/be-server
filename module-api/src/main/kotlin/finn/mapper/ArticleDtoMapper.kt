package finn.mapper

import finn.converter.getAbstractDateBefore
import finn.entity.query.ArticleQ
import finn.paging.PageResponse
import finn.response.article.ArticleListResponse

fun toDto(articleData: PageResponse<ArticleQ>): ArticleListResponse {
    val ArticleList = articleData.content.map {
        ArticleListResponse.ArticleDataResponse(
            it.id, it.title, it.description,
            it.shortCompanyName, it.thumbnailUrl, it.contentUrl,
            getAbstractDateBefore(it.publishedDate), it.source, it.sentiment, it.reasoning
        )
    }.toList()
    return ArticleListResponse(ArticleList, articleData.page, articleData.hasNext)
}