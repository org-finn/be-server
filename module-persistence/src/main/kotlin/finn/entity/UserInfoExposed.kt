package finn.entity

import finn.table.UserInfoTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class UserInfoExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserInfoExposed>(UserInfoTable)

    var oauthUserId by UserInfoTable.oauthUserId
    var nickname by UserInfoTable.nickname
    var role by UserInfoTable.role
    var status by UserInfoTable.status
    var favoriteTickers by UserInfoTable.favoriteTickers
    var createdAt by UserInfoTable.createdAt
    var updatedAt by UserInfoTable.updatedAt
    var deletedAt by UserInfoTable.deletedAt
}