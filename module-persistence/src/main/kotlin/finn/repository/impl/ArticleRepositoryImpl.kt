package finn.repository.impl

import finn.dto.ArticleToInsert
import finn.entity.command.ArticleC
import finn.entity.query.ArticleQ
import finn.exception.CriticalDataPollutedException
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.repository.ArticleRepository
import finn.repository.query.ArticleQueryRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ArticleRepositoryImpl(
    private val articleQueryRepository: ArticleQueryRepository
) : ArticleRepository {
    override fun getArticleDataForPredictionDetail(tickerId: UUID): List<ArticleDataQueryDto> {
        return articleQueryRepository.findArticleListByTickerId(tickerId)
    }

    override fun getArticleList(
        page: Int,
        size: Int,
        filter: String,
        sort: String
    ): PageResponse<ArticleQ> {
        val ArticleExposedList = when (filter) {
            "all" -> articleQueryRepository.findAllArticleList(page, size)

            "positive" -> articleQueryRepository.findAllPositiveArticleList(page, size)

            "negative" -> articleQueryRepository.findAllNegativeArticleList(page, size)

            else -> throw CriticalDataPollutedException("filter: $filter, 지원하지 않는 옵션입니다.")
        }
        return PageResponse(ArticleExposedList.content.map { it ->
            toDomain(it)
        }.toList(), page, size, ArticleExposedList.hasNext)
    }

    override fun saveArticleList(articleList: List<ArticleC>) {
        val articleToInsertList = articleList.asSequence()
            .map {
                ArticleToInsert(
                    it.title, it.description, it.thumbnailUrl, it.contentUrl, it.publishedDate,
                    it.shortCompanyName, it.source, it.distinctId, it.sentiment, it.reasoning,
                    it.tickerId, it.tickerCode
                )
            }
            .toList()
        articleQueryRepository.saveAll(articleToInsertList)
    }
}