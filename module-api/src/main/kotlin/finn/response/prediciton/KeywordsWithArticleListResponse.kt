package finn.response.prediciton

import java.util.*

data class KeywordsWithArticleListResponse(
    val keywords: List<KeywordsWithArticleResponse>
) {
    data class KeywordsWithArticleResponse(
        val keyword: String,
        val articles: List<UUID>,
        val sentiment: Int,
        val date: String
    )
}
