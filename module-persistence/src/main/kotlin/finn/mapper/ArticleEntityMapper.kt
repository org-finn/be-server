package finn.mapper

import finn.entity.ArticleExposed
import finn.entity.query.ArticleQ

fun toDomain(article: ArticleExposed): ArticleQ {
    return ArticleQ.create(
        article.id.value, article.title, article.description,
        article.thumbnailUrl, article.contentUrl, article.publishedDate,
        article.author, article.tickers
    )
}