package finn.queryDto

import java.util.*

interface ArticleTitleQueryDto {

    fun articleId(): UUID

    fun title(): String
}