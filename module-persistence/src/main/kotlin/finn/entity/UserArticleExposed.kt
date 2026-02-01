package finn.entity

import finn.table.UserArticleTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class UserArticleExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserArticleExposed>(UserArticleTable)

    var articleId by UserArticleTable.articleId
    var userId by UserArticleTable.userId
    var createdAt by UserArticleTable.createdAt
}