package finn.mapper

import finn.entity.Article
import finn.entity.ArticleExposed

fun toDomain(article: ArticleExposed): Article {
    return Article.create(
        article.id.value, article.title, article.description,
        article.thumbnailUrl, article.contentUrl, article.publishedDate, article.shortCompanyName,
        article.author, article.sentiment, article.reasoning, article.tickerId
    )
}