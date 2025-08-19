package finn.queryDto

import java.util.*

interface NewsDataQueryDto {

    fun newsId(): UUID

    fun headline(): String

    fun sentiment(): String

    fun reasoning(): String?
}