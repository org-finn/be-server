package finn.moduleApi.service

import finn.moduleApi.paging.NewsPageRequest
import finn.moduleDomain.entity.News
import finn.moduleDomain.paging.PageResponse
import finn.moduleDomain.queryDto.NewsDataQueryDto
import finn.moduleDomain.repository.NewsRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class NewsQueryService(private val newsRepository: NewsRepository) {

    fun getNewsDataForPredictionDetail(tickerId: UUID): List<NewsDataQueryDto> {
        return newsRepository.getNewsDataForPredictionDetail(tickerId)
    }

    fun getNewsDataList(pageRequest: NewsPageRequest): PageResponse<News> {
        return newsRepository.getNewsList(
            pageRequest.page,
            pageRequest.size,
            pageRequest.filter,
            pageRequest.sort
        )
    }
}