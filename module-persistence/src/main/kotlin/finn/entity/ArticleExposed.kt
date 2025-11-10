package finn.entity

import finn.table.ArticleTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ArticleExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ArticleExposed>(ArticleTable)

    var publishedDate by ArticleTable.publishedDate
    var title by ArticleTable.title
    var titleKr by ArticleTable.titleKr
    var description by ArticleTable.description
    var descriptionKr by ArticleTable.descriptionKr
    var contentUrl by ArticleTable.articleUrl
    var thumbnailUrl by ArticleTable.thumbnailUrl
    var viewCount by ArticleTable.viewCount
    var likeCount by ArticleTable.likeCount
    var author by ArticleTable.author
    var distinctId by ArticleTable.distinctId
    var tickers by ArticleTable.tickers
    var createdAt by ArticleTable.createdAt
}