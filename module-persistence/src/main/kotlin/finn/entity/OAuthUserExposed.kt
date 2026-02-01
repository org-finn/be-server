package finn.entity

import finn.table.OAuthUserTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class OAuthUserExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<OAuthUserExposed>(OAuthUserTable)

    var provider by OAuthUserTable.provider
    var providerId by OAuthUserTable.providerId
    var email by OAuthUserTable.email
    var createdAt by OAuthUserTable.createdAt
    var updatedAt by OAuthUserTable.updatedAt
}