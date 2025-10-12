package finn.repository.impl

import finn.entity.command.ArticleInsight
import finn.exception.CriticalDataPollutedException
import finn.insertDto.ArticleTickerToInsert
import finn.repository.ArticleTickerRepository
import finn.repository.exposed.ArticleTickerExposedRepository
import finn.repository.exposed.TickerExposedRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class ArticleTickerRepositoryImpl(
    private val articleTickerExposedRepository: ArticleTickerExposedRepository,
    private val tickerExposedRepository: TickerExposedRepository
) : ArticleTickerRepository {

    override fun saveArticleTicker(
        articleId: UUID,
        title: String,
        insights: List<ArticleInsight>,
        publishedDate: LocalDateTime
    ) {
        // id: ticker_code, value: ticker_id
        val tickerMap =
            tickerExposedRepository.findTickerMapByTickerCodeList(insights.map { it.tickerCode }
                .toList())

        val toInsert = insights.map {
            ArticleTickerToInsert(
                articleId,
                tickerMap[it.tickerCode]
                    ?: throw CriticalDataPollutedException("${it.tickerCode}는 지원하지 않는 종목입니다."),
                it.tickerCode,
                title,
                it.sentiment,
                it.reasoning,
                publishedDate
            )
        }.toList()
        articleTickerExposedRepository.saveAll(toInsert)
    }
}