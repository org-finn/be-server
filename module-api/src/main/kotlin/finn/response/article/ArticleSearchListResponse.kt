package finn.response.article

import finn.response.article.ArticleListResponse.ArticleDataResponse

data class ArticleSearchListResponse(
    val articles: List<ArticleDataResponse>
)
