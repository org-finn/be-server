package finn.service

import finn.entity.News
import finn.paging.NewsPageRequest
import finn.paging.PageResponse
import finn.queryDto.NewsDataQueryDto
import finn.repository.NewsRepository
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