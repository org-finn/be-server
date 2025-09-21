package finn.repository

import finn.entity.query.PredictionQ
import finn.entity.query.PredictionStrategy
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
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
        predictionDate: LocalDateTime
    )

    fun getPredictionList(page: Int, size: Int, sort: String): PageResponse<PredictionQueryDto>

    fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto

    suspend fun getRecentSentimentScoreList(tickerId: UUID): List<Int>

    suspend fun getRecentScore(tickerId: UUID): Int

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
}