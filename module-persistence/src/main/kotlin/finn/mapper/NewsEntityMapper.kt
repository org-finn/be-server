package finn.mapper

import finn.entity.News
import finn.entity.NewsExposed

fun toDomain(news: NewsExposed): News {
    return News.create(
        news.id.value, news.title, news.description,
        news.thumbnailUrl, news.contentUrl, news.publishedDate, news.shortCompanyName,
        news.author, news.sentiment, news.reasoning, news.tickerId
    )
}