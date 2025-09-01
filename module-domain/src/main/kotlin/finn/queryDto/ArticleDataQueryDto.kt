package finn.queryDto

import java.util.*

interface ArticleDataQueryDto {

    fun articleId(): UUID

    fun tickerId(): UUID

    fun headline(): String

    fun sentiment(): String?

    fun reasoning(): String?
}