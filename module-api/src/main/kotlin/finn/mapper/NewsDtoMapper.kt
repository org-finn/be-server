package finn.mapper

import finn.entity.News
import finn.paging.PageResponse
import finn.response.news.NewsListResponse

fun toDto(newsData: PageResponse<News>): NewsListResponse {
    val newsList = newsData.content.map { it ->
        NewsListResponse.NewsDataResponse(
            it.id, it.title, it.description,
            it.shortCompanyName, it.thumbnailUrl, it.contentUrl,
            it.publishedDate.toString(), it.source, it.sentiment, it.reasoning
        )
    }.toList()
    return NewsListResponse(newsList, newsData.page, newsData.hasNext)
}