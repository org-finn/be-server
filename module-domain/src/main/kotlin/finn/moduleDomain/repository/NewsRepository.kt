package finn.moduleDomain.repository

import finn.moduleDomain.entity.News
import finn.moduleDomain.paging.PageResponse
import finn.moduleDomain.queryDto.NewsDataQueryDto
import java.util.*

interface NewsRepository {

    fun getNewsDataForPredictionDetail(tickerId: UUID) : List<NewsDataQueryDto>

    fun getNewsList(page: Int, size: Int, filter: String, sort:String) : PageResponse<News>
}