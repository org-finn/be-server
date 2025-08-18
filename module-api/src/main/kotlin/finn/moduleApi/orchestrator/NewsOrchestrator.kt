package finn.moduleApi.orchestrator

import finn.moduleApi.mapper.NewsDtoMapper
import finn.moduleApi.paging.NewsPageRequest
import finn.moduleApi.response.news.NewsListResponse
import finn.moduleApi.service.NewsQueryService
import finn.moduleCommon.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional(readOnly = true)
class NewsOrchestrator(
    private val newsQueryService: NewsQueryService
) {

    fun getRecentNewsList(pageRequest: NewsPageRequest): NewsListResponse {
        val newsList = newsQueryService.getNewsDataList(pageRequest)
        return NewsDtoMapper.toDto(newsList)
    }
}