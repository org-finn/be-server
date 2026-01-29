package finn.repository.impl

import finn.entity.TickerScore
import finn.entity.query.PredictionQ
import finn.entity.query.PredictionStrategy
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.PredictionCreateDto
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import finn.queryDto.PredictionUpdateDto
import finn.repository.PredictionRepository
import finn.repository.dynamodb.TickerPriceRealTimeDynamoDbRepository
import finn.repository.exposed.PredictionExposedRepository
import finn.repository.exposed.UserInfoExposedRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Repository
class PredictionRepositoryImpl(
    private val userInfoExposedRepository: UserInfoExposedRepository,
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

    override suspend fun saveAll(predictions: List<PredictionCreateDto>) {
        predictionExposedRepository.batchInsertPredictions(predictions)
    }

    override fun getPredictionListDefault(
        page: Int,
        size: Int,
        sort: String,
        userId: UUID?
    ): PageResponse<PredictionQueryDto> {
        val predictionExposedList = predictionExposedRepository.findAllPrediction(page, size, sort)
        if (userId != null) {
            setFavoriteTicker(
                userId,
                predictionExposedList.content.map { it.tickerCode }.toList(),
                predictionExposedList.content
            )
        }
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
        sort: String,
        userId: UUID?
    ): PageResponse<PredictionQueryDto> {
        val predictionExposedList = predictionExposedRepository.findAllPrediction(page, size, sort)
        predictionExposedRepository.setPredictionDataForParam(
            "keyword",
            predictionExposedList.content
        )
        if (userId != null) {
            setFavoriteTicker(
                userId,
                predictionExposedList.content.map { it.tickerCode }.toList(),
                predictionExposedList.content
            )
        }

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
        sort: String,
        userId: UUID?
    ): PageResponse<PredictionQueryDto> {
        val predictionExposedList = predictionExposedRepository.findAllPrediction(page, size, sort)
        predictionExposedRepository.setPredictionDataForParam(
            "article",
            predictionExposedList.content
        )
        if (userId != null) {
            setFavoriteTicker(
                userId,
                predictionExposedList.content.map { it.tickerCode }.toList(),
                predictionExposedList.content
            )
        }

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
        isOpened: Boolean,
        userId: UUID?
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

        if (userId != null) {
            setFavoriteTicker(
                userId,
                predictionExposedList.content.map { it.tickerCode }.toList(),
                predictionExposedList.content
            )
        }

        return PageResponse(
            predictionExposedList.content,
            page,
            size,
            predictionExposedList.hasNext
        )
    }

    private fun setFavoriteTicker(
        userId: UUID, tickerCodes: List<String>,
        queryDto: List<PredictionQueryDto>
    ) {
        val map = getFavoriteResultMap(userId, tickerCodes)
        queryDto.forEach {
            it.isFavorite = map[it.tickerCode] == true
        }
    }

    private fun getFavoriteResultMap(
        userId: UUID,
        tickerCodes: List<String>
    ): Map<String, Boolean> {
        return userInfoExposedRepository.existFavorite(userId, tickerCodes)
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

    override suspend fun updateAll(predictions: List<PredictionUpdateDto>, alpha: Double) {
        predictionExposedRepository.batchUpdatePredictions(predictions, alpha)
    }

    override suspend fun getYesterdayVolatilityByTickerId(tickerId: UUID): BigDecimal {
        return predictionExposedRepository.findPreviousVolatilityByTickerId(tickerId)
    }

    override suspend fun findAllByTickerIds(tickerIds: List<UUID>): List<PredictionQ> {
        return predictionExposedRepository.findAllByTickerIds(tickerIds)
            .map { toDomain(it) }
            .toList()
    }

    override suspend fun findAll(): List<PredictionQ> {
        return predictionExposedRepository.findAll()
            .map { toDomain(it) }
            .toList()
    }

    override suspend fun findYesterdayVolatilityMap(tickerIds: List<UUID>): Map<UUID, BigDecimal> {
        return predictionExposedRepository.findYesterdayVolatilities(tickerIds)
    }
}