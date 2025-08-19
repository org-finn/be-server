package finn.modulePersistence.repository.facade

import finn.moduleDomain.entity.News
import finn.moduleDomain.paging.PageResponse
import finn.moduleDomain.queryDto.NewsDataQueryDto
import finn.moduleDomain.repository.NewsRepository
import finn.modulePersistence.repository.db.NewsExposedRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class NewsRepositoryImpl(
    private val newsExposedRepository: NewsExposedRepository
) : NewsRepository {
    override fun getNewsDataForPredictionDetail(tickerId: UUID): List<NewsDataQueryDto> {
        TODO("Not yet implemented")
    }

    override fun getNewsList(
        page: Int,
        size: Int,
        filter: String,
        sort: String
    ): PageResponse<News> {
        TODO("Not yet implemented")
    }
}