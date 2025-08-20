package finn.repository.impl

import finn.entity.News
import finn.exception.CriticalDataPollutedException
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.NewsDataQueryDto
import finn.repository.NewsRepository
import finn.repository.query.NewsQueryRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class NewsRepositoryImpl(
    private val newsQueryRepository: NewsQueryRepository
) : NewsRepository {
    override fun getNewsDataForPredictionDetail(tickerId: UUID): List<NewsDataQueryDto> {
        return newsQueryRepository.findNewsListByTickerId(tickerId)
    }

    override fun getNewsList(
        page: Int,
        size: Int,
        filter: String,
        sort: String
    ): PageResponse<News> {
        val newsExposedList = when (filter) {
            "all" -> newsQueryRepository.findAllNewsList(page, size)

            "positive" -> newsQueryRepository.findAllPositiveNewsList(page, size)

            "negative" -> newsQueryRepository.findAllNegativeNewsList(page, size)

            else -> throw CriticalDataPollutedException("filter: $filter, 지원하지 않는 옵션입니다.")
        }
        return PageResponse(newsExposedList.content.map { it ->
            toDomain(it)
        }.toList(), page, size, newsExposedList.hasNext)
    }

}