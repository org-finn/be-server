package finn.mapper

import finn.entity.ArticleExposed
import finn.entity.query.ArticleQ
import java.time.ZoneId

fun toDomain(article: ArticleExposed): ArticleQ {
    return ArticleQ.create(
        article.id.value,
        article.titleKr ?: article.title,
        article.descriptionKr ?: article.description,
        article.thumbnailUrl,
        article.contentUrl,
        article.publishedDate.atZone(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime(),
        article.author,
        article.tickers
    )
}