package finn.mapper

import finn.entity.Article
import finn.paging.PageResponse
import finn.response.article.ArticleListResponse

fun toDto(articleData: PageResponse<Article>): ArticleListResponse {
    val ArticleList = articleData.content.map { it ->
        ArticleListResponse.ArticleDataResponse(
            it.id, it.title, it.description,
            it.shortCompanyName, it.thumbnailUrl, it.contentUrl,
            it.publishedDate.toString(), it.source, it.sentiment, it.reasoning
        )
    }.toList()
    return ArticleListResponse(ArticleList, articleData.page, articleData.hasNext)
}