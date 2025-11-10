package finn.repository

import finn.entity.query.ArticleQ
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.ArticleDetailQueryDto
import java.util.*

interface ArticleRepository {

    fun getArticleDataForPredictionDetail(tickerId: UUID): List<ArticleDataQueryDto>

    fun getArticleList(
        page: Int,
        size: Int,
        tickerCodes: List<String>?,
        sentiment: String?,
        sort: String
    ): PageResponse<ArticleQ>

    fun getArticle(articleId: UUID): ArticleDetailQueryDto

}