package finn.entity

import finn.table.ExponentTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ExponentExposed(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ExponentExposed>(ExponentTable)

    var code by ExponentTable.code
    var name by ExponentTable.name
    var country by ExponentTable.country
}