package finn.repository

import finn.entity.News
import finn.paging.PageResponse
import finn.queryDto.NewsDataQueryDto
import java.util.*

interface NewsRepository {

    fun getNewsDataForPredictionDetail(tickerId: UUID) : List<NewsDataQueryDto>

    fun getNewsList(page: Int, size: Int, filter: String, sort:String) : PageResponse<News>
}