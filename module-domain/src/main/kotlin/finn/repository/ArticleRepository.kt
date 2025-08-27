package finn.repository

import finn.entity.Article
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import java.util.*

interface ArticleRepository {

    fun getArticleDataForPredictionDetail(tickerId: UUID) : List<ArticleDataQueryDto>

    fun getArticleList(page: Int, size: Int, filter: String, sort:String) : PageResponse<Article>

    fun saveArticleList(articleList: List<Article>)
}