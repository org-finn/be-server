package finn.response.prediciton

data class KeywordsWithArticleListResponse(
    val keywords: List<KeywordsWithArticleResponse>
) {
    data class KeywordsWithArticleResponse(
        val keyword: String,
        val articles: List<ArticleIdAndTitleResponse>,
        val sentiment: Int,
        val date: String
    )

    data class ArticleIdAndTitleResponse(
        val articleId: String,
        val title: String
    )
}
