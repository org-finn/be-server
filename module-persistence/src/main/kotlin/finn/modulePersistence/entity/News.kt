package finn.modulePersistence.entity

import finn.modulePersistence.table.NewsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class News(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<News>(NewsTable)

    var publishedDate by NewsTable.publishedDate
    var title by NewsTable.title
    var description by NewsTable.description
    var newsUrl by NewsTable.newsUrl
    var imageUrl by NewsTable.imageUrl
    var viewCount by NewsTable.viewCount
    var likeCount by NewsTable.likeCount
    var sentiment by NewsTable.sentiment
    var sentimentReasoning by NewsTable.sentimentReasoning
    var shortCompanyName by NewsTable.shortCompanyName
    var author by NewsTable.author
    var distinctId by NewsTable.distinctId
    var ticker by NewsTable.tickerId
    var tickerCode by NewsTable.tickerCode
    var createdAt by NewsTable.createdAt
}