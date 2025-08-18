package finn.moduleApi.repository

import finn.moduleApi.paging.NewsPageRequest
import finn.moduleApi.paging.PageResponse
import finn.moduleApi.queryDto.NewsDataQueryDto
import finn.moduleDomain.entity.News
import java.util.*

interface NewsRepository {

    fun getNewsDataForPredictionDetail(tickerId: UUID) : List<NewsDataQueryDto>

    fun getNewsList(pageRequest: NewsPageRequest) : PageResponse<News>
}