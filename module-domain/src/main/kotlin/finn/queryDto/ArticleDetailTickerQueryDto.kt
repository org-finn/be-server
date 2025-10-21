package finn.queryDto

interface ArticleDetailTickerQueryDto {
    fun shortCompanyName(): String

    fun sentiment(): String?

    fun reasoning(): String?

}