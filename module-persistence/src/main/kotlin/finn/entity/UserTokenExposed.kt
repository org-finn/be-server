package finn.entity

import finn.table.UserTokenTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class UserTokenExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserTokenExposed>(UserTokenTable)

    var userInfoId by UserTokenTable.userInfoId
    var deviceId by UserTokenTable.deviceId
    var deviceType by UserTokenTable.deviceType
    var refreshToken by UserTokenTable.refreshToken
    var createdAt by UserTokenTable.createdAt
}