package finn.response.search

import finn.response.article.ArticleListResponse.ArticleDataResponse

data class ArticleSearchListResponse(
    val articles: List<ArticleDataResponse>,
    val isMore: Boolean
)