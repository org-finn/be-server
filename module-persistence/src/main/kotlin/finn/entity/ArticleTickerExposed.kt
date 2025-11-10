package finn.entity

import finn.table.ArticleTickerTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ArticleTickerExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ArticleTickerExposed>(ArticleTickerTable)

    var articleId by ArticleTickerTable.articleId
    var tickerId by ArticleTickerTable.tickerId
    var tickerCode by ArticleTickerTable.tickerCode
    var shortCompanyName by ArticleTickerTable.shortCompanyName
    var title by ArticleTickerTable.title
    var titleKr by ArticleTickerTable.titleKr
    var sentiment by ArticleTickerTable.sentiment
    var reasoning by ArticleTickerTable.reasoning
    var reasoningKr by ArticleTickerTable.reasoningKr
    var publishedDate by ArticleTickerTable.publishedDate
    var createdAt by ArticleTickerTable.createdAt
}