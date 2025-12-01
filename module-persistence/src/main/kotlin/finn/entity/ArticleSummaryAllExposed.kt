package finn.entity

import finn.table.ArticleSummaryAllTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ArticleSummaryAllExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ArticleSummaryAllExposed>(ArticleSummaryAllTable)
    var summaryDate by ArticleSummaryAllTable.summaryDate
    var positiveReasoning by ArticleSummaryAllTable.positiveReasoning
    var negativeReasoning by ArticleSummaryAllTable.negativeReasoning
    var positiveKeywords by ArticleSummaryAllTable.positiveKeywords
    var negativeKeywords by ArticleSummaryAllTable.negativeKeywords
}