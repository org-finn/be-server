package finn.response.userinfo

import finn.response.article.ArticleListResponse.ArticleDataResponse

data class FavoriteArticleResponse(
    val articles: List<ArticleDataResponse>
)
