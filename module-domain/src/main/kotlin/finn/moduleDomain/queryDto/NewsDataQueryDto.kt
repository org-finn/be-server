package finn.moduleDomain.queryDto

import java.util.*

interface NewsDataQueryDto {

    fun getNewsId(): UUID

    fun getHeadline(): String

    fun getSentiment(): String

    fun getReasoning(): String?
}