package finn.entity

import finn.table.ArticleSummaryTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ArticleSummaryExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ArticleSummaryExposed>(ArticleSummaryTable)
    var tickerId by ArticleSummaryTable.tickerId
    var shortCompanyName by ArticleSummaryTable.shortCompanyName
    var summaryDate = ArticleSummaryTable.summaryDate
    var positiveReasoning = ArticleSummaryTable.positiveReasoning
    var negativeReasoning = ArticleSummaryTable.negativeReasoning
    var positiveKeywords = ArticleSummaryTable.positiveKeywords
    var negativeKeywords = ArticleSummaryTable.negativeKeywords
}