package finn.entity

import finn.table.ArticleSummaryAllTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ArticleSummaryAllExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ArticleSummaryAllExposed>(ArticleSummaryAllTable)
    var summaryDate = ArticleSummaryAllTable.summaryDate
    var positiveReasoning = ArticleSummaryAllTable.positiveReasoning
    var negativeReasoning = ArticleSummaryAllTable.negativeReasoning
    var positiveKeywords = ArticleSummaryAllTable.positiveKeywords
    var negativeKeywords = ArticleSummaryAllTable.negativeKeywords
}