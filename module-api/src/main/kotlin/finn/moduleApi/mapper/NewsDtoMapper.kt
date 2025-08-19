package finn.moduleApi.mapper

import finn.moduleApi.response.news.NewsListResponse
import finn.moduleDomain.entity.News
import finn.moduleDomain.paging.PageResponse

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