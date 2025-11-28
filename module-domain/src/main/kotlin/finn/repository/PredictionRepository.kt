package finn.repository

import finn.entity.TickerScore
import finn.entity.query.PredictionQ
import finn.entity.query.PredictionStrategy
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

interface PredictionRepository {

    suspend fun save(
        tickerId: UUID,
        tickerCode: String,
        shortCompanyName: String,
        sentiment: Int,
        strategy: PredictionStrategy,
        score: Int,
        volatility: BigDecimal,
        predictionDate: LocalDateTime
    )


    fun getPredictionListDefault(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<PredictionQueryDto>

    fun getPredictionListWithKeyword(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<PredictionQueryDto>

    fun getPredictionListWithArticle(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<PredictionQueryDto>

    fun getPredictionListWithGraph(
        page: Int,
        size: Int,
        sort: String,
        isOpened: Boolean
    ): PageResponse<PredictionQueryDto>

    fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto

    suspend fun getRecentSentimentScoreList(tickerId: UUID): List<Int>

    suspend fun getAllTickerRecentScore(): List<TickerScore>

    suspend fun getRecentScoreByTickerId(tickerId: UUID): Int

    suspend fun updatePredictionByArticle(
        tickerId: UUID,
        predictionDate: LocalDateTime,
        positiveArticleCount: Long,
        negativeArticleCount: Long,
        neutralArticleCount: Long,
        score: Int,
        sentiment: Int,
        strategy: String
    ): PredictionQ

    suspend fun updatePredictionByExponent(
        predictionDate: LocalDateTime,
        scores: List<TickerScore>
    )

    suspend fun getYesterdayVolatilityByTickerId(tickerId: UUID): BigDecimal
}