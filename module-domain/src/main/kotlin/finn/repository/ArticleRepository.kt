package finn.repository

import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.ArticleDetailQueryDto
import finn.queryDto.PredictionArticleDataQueryDto
import java.util.*

interface ArticleRepository {

    fun getArticleDataForPredictionDetail(tickerId: UUID): List<PredictionArticleDataQueryDto>

    fun getArticleList(
        userId: UUID?,
        page: Int,
        size: Int,
        tickerCodes: List<String>?,
        sentiment: String?,
        sort: String
    ): PageResponse<ArticleDataQueryDto>

    fun getArticle(userId: UUID?, articleId: UUID): ArticleDetailQueryDto

    fun findArticleListByKeyword(keyword: String): List<ArticleDataQueryDto>

}