package finn.moduleApi.response.news

import java.util.*

data class NewsListResponse(
    val newsList: List<NewsDataResponse>,
    val pageNumber: Int,
    val hasNext: Boolean
) {
    data class NewsDataResponse(
        val newsId: UUID,
        val title: String,
        val description: String,
        val shortCompanyName: String,
        val thumbnailUrl: String,
        val contentUrl: String,
        val publishedDate: String,
        val source: String,
        val sentiment: String,
        val reasoning: String? = null
    )
}
