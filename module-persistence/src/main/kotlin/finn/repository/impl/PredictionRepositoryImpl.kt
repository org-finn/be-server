package finn.repository.impl

import finn.entity.TickerScore
import finn.entity.query.PredictionQ
import finn.entity.query.PredictionStrategy
import finn.exception.CriticalDataPollutedException
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import finn.repository.PredictionRepository
import finn.repository.exposed.PredictionExposedRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class PredictionRepositoryImpl(
    private val predictionExposedRepository: PredictionExposedRepository
) : PredictionRepository {

    override suspend fun save(
        tickerId: UUID,
        tickerCode: String,
        shortCompanyName: String,
        sentiment: Int,
        strategy: PredictionStrategy,
        score: Int,
        predictionDate: LocalDateTime
    ) {
        predictionExposedRepository.save(
            tickerId,
            tickerCode,
            shortCompanyName,
            sentiment,
            strategy.strategy,
            score,
            predictionDate
        )
    }

    override fun getPredictionList(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<PredictionQueryDto> {
        val predictionExposedList = when (sort) {
            "popular" -> predictionExposedRepository.findAllPredictionByPopular(
                page,
                size
            )

            "upward" -> predictionExposedRepository.findAllPredictionBySentimentScore(
                page,
                size,
                false
            )

            "downward" -> predictionExposedRepository.findAllPredictionBySentimentScore(
                page,
                size,
                true
            )

            else -> throw CriticalDataPollutedException("Sort: $sort, 지원하지 않는 옵션입니다.")
        }
        return PageResponse(
            predictionExposedList.content,
            page,
            size,
            predictionExposedList.hasNext
        )
    }

    override fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto {
        return predictionExposedRepository.findPredictionWithPriceInfoById(tickerId)
    }

    override suspend fun getRecentSentimentScoreList(tickerId: UUID): List<Int> {
        return predictionExposedRepository.findTodaySentimentScoreListByTickerId(tickerId)
    }

    override suspend fun getRecentScoreByTickerId(tickerId: UUID): Int {
        return predictionExposedRepository.findTodaySentimentScoreByTickerId(tickerId)
    }

    override suspend fun getAllTickerRecentScore(): List<TickerScore> {
        return predictionExposedRepository.findTodaySentimentScoreList()
    }

    override suspend fun updatePredictionByExponent(
        predictionDate: LocalDateTime,
        scores: List<TickerScore>
    ) {
        predictionExposedRepository.updateByExponent(predictionDate, scores)
    }

    override suspend fun updatePredictionByArticle(
        tickerId: UUID,
        predictionDate: LocalDateTime,
        positiveArticleCount: Long,
        negativeArticleCount: Long,
        neutralArticleCount: Long,
        score: Int,
        sentiment: Int,
        strategy: String
    ): PredictionQ {
        return toDomain(
            predictionExposedRepository.updateByArticle(
                tickerId, predictionDate, positiveArticleCount, negativeArticleCount,
                neutralArticleCount, score, sentiment, strategy
            )
        )
    }
}