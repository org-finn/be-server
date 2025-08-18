package finn.moduleApi.service

import finn.moduleApi.paging.NewsPageRequest
import finn.moduleApi.paging.PageResponse
import finn.moduleApi.queryDto.NewsDataQueryDto
import finn.moduleApi.repository.NewsRepository
import finn.moduleDomain.entity.News
import org.springframework.stereotype.Service
import java.util.*

@Service
class NewsQueryService(private val newsRepository: NewsRepository) {

    fun getNewsDataForPredictionDetail(tickerId: UUID) : List<NewsDataQueryDto> {
        return newsRepository.getNewsDataForPredictionDetail(tickerId)
    }

    fun getNewsDataList(pageRequest: NewsPageRequest) : PageResponse<News> {
        return newsRepository.getNewsList(pageRequest)
    }
}