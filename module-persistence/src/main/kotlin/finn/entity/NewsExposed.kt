package finn.entity

import finn.table.NewsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class NewsExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<NewsExposed>(NewsTable)

    var publishedDate by NewsTable.publishedDate
    var title by NewsTable.title
    var description by NewsTable.description
    var contentUrl by NewsTable.newsUrl
    var thumbnailUrl by NewsTable.thumbnailUrl
    var viewCount by NewsTable.viewCount
    var likeCount by NewsTable.likeCount
    var sentiment by NewsTable.sentiment
    var reasoning by NewsTable.reasoning
    var shortCompanyName by NewsTable.shortCompanyName
    var author by NewsTable.author
    var distinctId by NewsTable.distinctId
    var tickerId by NewsTable.tickerId
    var tickerCode by NewsTable.tickerCode
    var createdAt by NewsTable.createdAt
}