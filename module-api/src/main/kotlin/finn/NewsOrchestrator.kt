package finn.orchestrator

import finn.mapper.toDto
import finn.paging.NewsPageRequest
import finn.response.news.NewsListResponse
import finn.service.NewsQueryService
import finn.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional(readOnly = true)
class NewsOrchestrator(
    private val newsQueryService: NewsQueryService
) {

    fun getRecentNewsList(pageRequest: NewsPageRequest): NewsListResponse {
        val newsList = newsQueryService.getNewsDataList(pageRequest)
        return toDto(newsList)
    }
}