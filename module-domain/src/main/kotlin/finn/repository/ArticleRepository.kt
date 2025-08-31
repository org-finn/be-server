package finn.repository

import finn.entity.command.ArticleC
import finn.entity.command.ArticleInsight
import finn.entity.query.ArticleQ
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import java.util.*

interface ArticleRepository {

    fun getArticleDataForPredictionDetail(tickerId: UUID) : List<ArticleDataQueryDto>

    fun getArticleList(page: Int, size: Int, filter: String, sort:String) : PageResponse<ArticleQ>

    fun saveArticle(article: ArticleC, insights: List<ArticleInsight>) : UUID
}