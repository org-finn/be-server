package finn.moduledata

import org.jetbrains.exposed.sql.Table

object DemoTable : Table("demo_table") {
    val id = integer("id").autoIncrement()

}