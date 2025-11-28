package finn.repository.impl

import finn.entity.TickerScore
import finn.entity.query.PredictionQ
import finn.entity.query.PredictionStrategy
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import finn.repository.PredictionRepository
import finn.repository.dynamodb.TickerPriceRealTimeDynamoDbRepository
import finn.repository.exposed.PredictionExposedRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Repository
class PredictionRepositoryImpl(
    private val predictionExposedRepository: PredictionExposedRepository,
    private val tickerPriceRealTimeDynamoDbRepository: TickerPriceRealTimeDynamoDbRepository
) : PredictionRepository {

    override suspend fun save(
        tickerId: UUID,
        tickerCode: String,
        shortCompanyName: String,
        sentiment: Int,
        strategy: PredictionStrategy,
        score: Int,
        volatility: BigDecimal,
        predictionDate: LocalDateTime
    ) {
        predictionExposedRepository.save(
            tickerId,
            tickerCode,
            shortCompanyName,
            sentiment,
            strategy.strategy,
            score,
            volatility,
            predictionDate
        )
    }

    override fun getPredictionListDefault(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<PredictionQueryDto> {
        val predictionExposedList = predictionExposedRepository.findAllPrediction(page, size, sort)

        return PageResponse(
            predictionExposedList.content,
            page,
            size,
            predictionExposedList.hasNext
        )
    }

    override fun getPredictionListWithKeyword(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<PredictionQueryDto> {
        val predictionExposedList = predictionExposedRepository.findAllPrediction(page, size, sort)
        predictionExposedRepository.setPredictionDataForParam(
            "keyword",
            predictionExposedList.content
        )

        return PageResponse(
            predictionExposedList.content,
            page,
            size,
            predictionExposedList.hasNext
        )
    }

    override fun getPredictionListWithArticle(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<PredictionQueryDto> {
        val predictionExposedList = predictionExposedRepository.findAllPrediction(page, size, sort)
        predictionExposedRepository.setPredictionDataForParam(
            "article",
            predictionExposedList.content
        )

        return PageResponse(
            predictionExposedList.content,
            page,
            size,
            predictionExposedList.hasNext
        )
    }

    override fun getPredictionListWithGraph(
        page: Int,
        size: Int,
        sort: String,
        isOpened: Boolean
    ): PageResponse<PredictionQueryDto> {
        val predictionExposedList = predictionExposedRepository.findAllPrediction(page, size, sort)

        if (!isOpened) {
            predictionExposedRepository.setPredictionDataForParam(
                "graph",
                predictionExposedList.content
            )
        } else {
            predictionExposedList.content.forEach {
                tickerPriceRealTimeDynamoDbRepository.setLatestRealTimeDataForPrediction(it)
            }
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

    override suspend fun getYesterdayVolatilityByTickerId(tickerId: UUID): BigDecimal {
        return predictionExposedRepository.findPreviousVolatilityByTickerId(tickerId)
    }
}