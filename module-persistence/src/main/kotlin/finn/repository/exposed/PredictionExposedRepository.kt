package finn.repository.exposed

import finn.entity.PredictionExposed
import finn.entity.TickerScore
import finn.exception.CriticalDataOmittedException
import finn.exception.CriticalDataPollutedException
import finn.paging.PageResponse
import finn.queryDto.ArticleTitleQueryDto
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionListGraphDataQueryDto
import finn.queryDto.PredictionQueryDto
import finn.table.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Repository
class PredictionExposedRepository {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    suspend fun save(
        tickerId: UUID,
        tickerCode: String,
        shortCompanyName: String,
        sentiment: Int,
        strategy: String,
        score: Int,
        volatility: BigDecimal,
        predictionDate: LocalDateTime
    ): PredictionExposed {
        return PredictionExposed.new {
            this.predictionDate = predictionDate
            this.positiveArticleCount = 0L
            this.negativeArticleCount = 0L
            this.neutralArticleCount = 0L
            this.sentiment = sentiment
            this.strategy = strategy
            this.score = score
            this.volatility = volatility
            this.tickerCode = tickerCode
            this.shortCompanyName = shortCompanyName
            this.tickerId = tickerId
            this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        }
    }

    private data class PredictionQueryDtoImpl(
        val predictionDate: LocalDateTime,
        val tickerId: UUID,
        val shortCompanyName: String,
        val tickerCode: String,
        val predictionStrategy: String,
        val sentiment: Int,
        val articleCount: Long,
        var positiveKeywords: String?,
        var negativeKeywords: String?,
        var articleTitles: List<ArticleTitleQueryDto>?,
        var graphData: PredictionListGraphDataQueryDto?
    ) : PredictionQueryDto {
        override fun predictionDate(): LocalDateTime = this.predictionDate
        override fun tickerId(): UUID = this.tickerId
        override fun shortCompanyName(): String = this.shortCompanyName
        override fun tickerCode(): String = this.tickerCode
        override fun predictionStrategy(): String = this.predictionStrategy
        override fun sentiment(): Int = this.sentiment
        override fun articleCount(): Long = this.articleCount
        override fun positiveKeywords(): String? = this.positiveKeywords
        override fun negativeKeywords(): String? = this.negativeKeywords
        override fun articleTitles(): List<ArticleTitleQueryDto>? = this.articleTitles
        override fun graphData(): PredictionListGraphDataQueryDto? = this.graphData
    }

    private data class ArticleTitleQueryDtoImpl(
        val articleId: UUID,
        val title: String
    ) : ArticleTitleQueryDto {
        override fun articleId(): UUID = this.articleId
        override fun title(): String = this.title
    }

    private data class PredictionListGraphDataQueryDtoImpl(
        val marketOpen: Boolean,
        val priceData: List<BigDecimal>
    ) : PredictionListGraphDataQueryDto {
        override fun marketOpen(): Boolean = this.marketOpen
        override fun priceData(): List<BigDecimal> = this.priceData
    }

    fun findAllPredictionByPopular(
        page: Int,
        size: Int,
        param: String?
    ): PageResponse<PredictionQueryDto> {

        val maxDateExpression = PredictionTable.predictionDate.max()
        val latestDate = PredictionTable
            .select(maxDateExpression)
            .firstOrNull()
            ?.get(maxDateExpression)
            ?: throw CriticalDataOmittedException("치명적 오류: 주가 정보가 존재하지 않습니다.")

        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val query = PredictionTable
            .join(
                TickerTable, JoinType.INNER,
                PredictionTable.tickerId,
                TickerTable.id
            )
            .select(PredictionTable.columns)
            .where(PredictionTable.predictionDate eq latestDate)
            .orderBy(
                TickerTable.marketCap to SortOrder.DESC,
                PredictionTable.tickerCode to SortOrder.ASC
            )
            .limit(n = itemsToFetch, offset = offset)

        val results = query.map { row ->
            val articleCount = when (row[PredictionTable.sentiment]) {
                1 -> row[PredictionTable.positiveArticleCount]
                -1 -> row[PredictionTable.negativeArticleCount]
                else -> row[PredictionTable.neutralArticleCount] // 0
            }

            PredictionQueryDtoImpl(
                predictionDate = row[PredictionTable.predictionDate],
                tickerId = row[PredictionTable.tickerId],
                shortCompanyName = row[PredictionTable.shortCompanyName],
                tickerCode = row[PredictionTable.tickerCode],
                predictionStrategy = row[PredictionTable.strategy],
                sentiment = row[PredictionTable.sentiment],
                articleCount = articleCount,
                positiveKeywords = null,
                negativeKeywords = null,
                articleTitles = null,
                graphData = null
            )
        }
        setParamData(param, results)

        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(
            content = content,
            page = page,
            size = content.size,
            hasNext = hasNext
        )
    }



    fun findAllPredictionBySentimentScore(
        page: Int,
        size: Int,
        isDownward: Boolean,
        param: String?
    ): PageResponse<PredictionQueryDto> {

        val maxDateExpression = PredictionTable.predictionDate.max()
        val latestDate = PredictionTable
            .select(maxDateExpression)
            .firstOrNull()
            ?.get(maxDateExpression)
            ?: throw CriticalDataOmittedException("치명적 오류: 주가 정보가 존재하지 않습니다.")

        val sortOrder = if (isDownward) SortOrder.DESC else SortOrder.ASC

        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val query = PredictionTable
            .selectAll()
            .where(PredictionTable.predictionDate eq latestDate)
            .orderBy(
                PredictionTable.score to sortOrder,
                PredictionTable.tickerCode to SortOrder.ASC
            )
            .limit(n = itemsToFetch, offset = offset)

        val results = query.map { row ->
            val articleCount = when (row[PredictionTable.sentiment]) {
                1 -> row[PredictionTable.positiveArticleCount]
                -1 -> row[PredictionTable.negativeArticleCount]
                else -> row[PredictionTable.neutralArticleCount] // 0
            }

            PredictionQueryDtoImpl(
                predictionDate = row[PredictionTable.predictionDate],
                tickerId = row[PredictionTable.tickerId],
                shortCompanyName = row[PredictionTable.shortCompanyName],
                tickerCode = row[PredictionTable.tickerCode],
                predictionStrategy = row[PredictionTable.strategy],
                sentiment = row[PredictionTable.sentiment],
                articleCount = articleCount,
                positiveKeywords = null,
                negativeKeywords = null,
                articleTitles = null,
                graphData = null
            )
        }
        setParamData(param, results)

        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(
            content = content,
            page = page,
            size = content.size,
            hasNext = hasNext
        )
    }

    fun findAllPredictionByVolatility(
        page: Int,
        size: Int,
        param: String?
    ): PageResponse<PredictionQueryDto> {
        val maxDateExpression = PredictionTable.predictionDate.max()
        val latestDate = PredictionTable
            .select(maxDateExpression)
            .firstOrNull()
            ?.get(maxDateExpression)
            ?: throw CriticalDataOmittedException("치명적 오류: 주가 정보가 존재하지 않습니다.")

        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val query = PredictionTable
            .selectAll()
            .where(PredictionTable.predictionDate eq latestDate)
            .orderBy(
                PredictionTable.volatility to SortOrder.DESC,
                PredictionTable.tickerCode to SortOrder.ASC
            )
            .limit(n = itemsToFetch, offset = offset)

        val results = query.map { row ->
            val articleCount = when (row[PredictionTable.sentiment]) {
                1 -> row[PredictionTable.positiveArticleCount]
                -1 -> row[PredictionTable.negativeArticleCount]
                else -> row[PredictionTable.neutralArticleCount] // 0
            }

            PredictionQueryDtoImpl(
                predictionDate = row[PredictionTable.predictionDate],
                tickerId = row[PredictionTable.tickerId],
                shortCompanyName = row[PredictionTable.shortCompanyName],
                tickerCode = row[PredictionTable.tickerCode],
                predictionStrategy = row[PredictionTable.strategy],
                sentiment = row[PredictionTable.sentiment],
                articleCount = articleCount,
                positiveKeywords = null,
                negativeKeywords = null,
                articleTitles = null,
                graphData = null
            )
        }
        setParamData(param, results)

        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(
            content = content,
            page = page,
            size = content.size,
            hasNext = hasNext
        )
    }

    private data class PredictionDetailQueryDtoImpl(
        val predictionDate: LocalDateTime,
        val tickerId: UUID,
        val shortCompanyName: String,
        val tickerCode: String,
        val predictionStrategy: String,
        val sentiment: Int,
        val articleCount: Long,
        val sentimentScore: Int,
        val priceDate: LocalDate,
        val open: BigDecimal,
        val close: BigDecimal,
        val high: BigDecimal,
        val low: BigDecimal,
        val volume: Long
    ) : PredictionDetailQueryDto {
        override fun predictionDate(): LocalDateTime = this.predictionDate
        override fun tickerId(): UUID = this.tickerId
        override fun shortCompanyName(): String = this.shortCompanyName
        override fun tickerCode(): String = this.tickerCode
        override fun predictionStrategy(): String = this.predictionStrategy
        override fun sentiment(): Int = this.sentiment
        override fun articleCount(): Long = this.articleCount
        override fun sentimentScore(): Int = this.sentimentScore
        override fun priceDate(): LocalDate = this.priceDate
        override fun open(): BigDecimal = this.open
        override fun close(): BigDecimal = this.close
        override fun high(): BigDecimal = this.high
        override fun low(): BigDecimal = this.low
        override fun volume(): Long = this.volume
    }

    fun findPredictionWithPriceInfoById(tickerId: UUID): PredictionDetailQueryDto {
        val latestPredictionDate = PredictionTable
            .select(PredictionTable.predictionDate.max())
            .where(PredictionTable.tickerId eq tickerId)
            .firstOrNull()
            ?.get(PredictionTable.predictionDate.max())?.toLocalDate()
            ?: throw CriticalDataOmittedException("치명적 오류: 예측 정보가 존재하지 않습니다.")
        val latestPriceDate = TickerPriceTable
            .select(TickerPriceTable.priceDate.max())
            .where(TickerPriceTable.tickerId eq tickerId)
            .firstOrNull()
            ?.get(TickerPriceTable.priceDate.max())?.toLocalDate()
            ?: throw CriticalDataOmittedException("치명적 오류: 주가 정보가 존재하지 않습니다.")

        return PredictionTable
            .join(
                TickerPriceTable, JoinType.INNER,
                additionalConstraint = {
                    (PredictionTable.tickerId eq TickerPriceTable.tickerId)
                }
            )
            .select(
                PredictionTable.predictionDate,
                PredictionTable.tickerId,
                PredictionTable.shortCompanyName,
                PredictionTable.tickerCode,
                PredictionTable.strategy,
                PredictionTable.sentiment,
                PredictionTable.score,
                PredictionTable.positiveArticleCount,
                PredictionTable.negativeArticleCount,
                PredictionTable.neutralArticleCount,
                TickerPriceTable.priceDate,
                TickerPriceTable.open,
                TickerPriceTable.close,
                TickerPriceTable.high,
                TickerPriceTable.low,
                TickerPriceTable.volume
            )
            .where {
                (PredictionTable.tickerId eq tickerId) and
                        (PredictionTable.predictionDate.date() eq latestPredictionDate) and
                        (TickerPriceTable.priceDate.date() eq latestPriceDate)
            }
            .limit(1)
            .map { row ->
                val articleCount = when (row[PredictionTable.sentiment]) {
                    1 -> row[PredictionTable.positiveArticleCount]
                    -1 -> row[PredictionTable.negativeArticleCount]
                    else -> row[PredictionTable.neutralArticleCount] // 0
                }
                PredictionDetailQueryDtoImpl(
                    predictionDate = row[PredictionTable.predictionDate],
                    tickerId = row[PredictionTable.tickerId],
                    shortCompanyName = row[PredictionTable.shortCompanyName],
                    tickerCode = row[PredictionTable.tickerCode],
                    predictionStrategy = row[PredictionTable.strategy],
                    sentiment = row[PredictionTable.sentiment],
                    articleCount = articleCount,
                    sentimentScore = row[PredictionTable.score],
                    priceDate = row[TickerPriceTable.priceDate].toLocalDate(),
                    open = row[TickerPriceTable.open],
                    close = row[TickerPriceTable.close],
                    high = row[TickerPriceTable.high],
                    low = row[TickerPriceTable.low],
                    volume = row[TickerPriceTable.volume]
                )
            }.singleOrNull()
            ?: throw CriticalDataOmittedException("치명적 오류: ${tickerId}에 대한 예측 상세 정보가 존재하지 않습니다.")
    }

    suspend fun updateByArticle(
        tickerId: UUID,
        predictionDate: LocalDateTime,
        newPositiveArticleCount: Long,
        newNegativeArticleCount: Long,
        newNeutralArticleCount: Long,
        score: Int,
        sentiment: Int,
        strategy: String
    ): PredictionExposed {
        return PredictionExposed.findSingleByAndUpdate(
            (PredictionTable.tickerId eq tickerId) and (PredictionTable.predictionDate eq predictionDate)
        ) {
            it.positiveArticleCount += newPositiveArticleCount
            it.negativeArticleCount += newNegativeArticleCount
            it.neutralArticleCount += newNeutralArticleCount
            it.score = score
            it.sentiment = sentiment
            it.strategy = strategy
        } ?: throw CriticalDataOmittedException("금일 일자로 생성된 ${tickerId}의 Prediction이 존재하지 않습니다.")
    }

    suspend fun updateByExponent(
        predictionDate: LocalDateTime,
        scores: List<TickerScore>
    ) {
        scores.forEach { scoreData ->
            PredictionTable.update({
                (PredictionTable.tickerId eq scoreData.tickerId) and (PredictionTable.predictionDate eq predictionDate)
            }) {
                it[score] = scoreData.score
                it[sentiment] = scoreData.sentiment
                it[strategy] = scoreData.strategy.strategy
            }
        }
    }

    // 최근 6일 간의 prediction score를 반환(추세 반영 목적)
    suspend fun findTodaySentimentScoreListByTickerId(tickerId: UUID): List<Int> {
        val today = LocalDate.now(ZoneId.of("America/New_York"))
        val sevenDaysAgo = today.minusDays(6) // 오늘을 제외한 이전 6일

        return PredictionTable
            .select(PredictionTable.score)
            .where {
                (PredictionTable.tickerId eq tickerId) and
                        (PredictionTable.predictionDate.date() greaterEq sevenDaysAgo)
            }
            .map { row ->
                row[PredictionTable.score]
            }
    }

    suspend fun findTodaySentimentScoreByTickerId(tickerId: UUID): Int {
        val today = LocalDate.now(ZoneId.of("America/New_York"))

        return PredictionTable
            .select(PredictionTable.score)
            .where {
                (PredictionTable.tickerId eq tickerId) and
                        (PredictionTable.predictionDate.date() eq today)
            }
            .map { row ->
                row[PredictionTable.score]
            }.singleOrNull()
            ?: throw CriticalDataOmittedException("금일 일자로 생성된 ${tickerId}의 Prediction이 존재하지 않습니다.")
    }

    suspend fun findTodaySentimentScoreList(): List<TickerScore> {
        val today = LocalDate.now(ZoneId.of("America/New_York"))

        return PredictionTable
            .select(PredictionTable.tickerId, PredictionTable.score)
            .where {
                (PredictionTable.predictionDate.date() eq today)
            }
            .map { row ->
                TickerScore(
                    row[PredictionTable.tickerId],
                    row[PredictionTable.score]
                )
            }
    }

    suspend fun findPreviousVolatilityByTickerId(tickerId: UUID): BigDecimal {
        return PredictionTable.select(PredictionTable.volatility)
            .where { PredictionTable.tickerId eq tickerId }
            .orderBy(PredictionTable.predictionDate, SortOrder.DESC)
            .limit(1)
            .map {
                it[PredictionTable.volatility]
            }.singleOrNull()
            ?: throw CriticalDataOmittedException("${tickerId}의 전일 변동성 지표 값이 존재하지 않습니다.")
    }

    /**
     * key: tickerId, value: positiveKeywords, negativeKeywords
     */
    private fun findArticleSummaryKeywordsForPrediction(): Map<UUID, List<String?>> {
        val result = ArticleSummaryTable.select(
            ArticleSummaryTable.tickerId,
            ArticleSummaryTable.positiveKeywords,
            ArticleSummaryTable.negativeKeywords
        ).where { ArticleSummaryTable.summaryDate.date() eq LocalDateTime.now().toLocalDate() }

        return result.associate { row ->
            row[ArticleSummaryTable.tickerId] to listOf(
                row[ArticleSummaryTable.positiveKeywords],
                row[ArticleSummaryTable.negativeKeywords]
            )
        }
    }

    private fun setParamData(
        param: String?,
        results: List<PredictionQueryDtoImpl>
    ) {
        param.let {
            when (param) {
                "keyword" -> {
                    val data = findArticleSummaryKeywordsForPrediction()
                    results.forEach { dtoImpl ->
                        val tickerId = dtoImpl.tickerId
                        data[tickerId]?.let {
                            dtoImpl.positiveKeywords = it[0]
                            dtoImpl.negativeKeywords = it[1]
                        }
                    }
                }

                "article" -> {
                    val data = findArticleTitlesForPrediction()
                    results.forEach { dtoImpl ->
                        val tickerId = dtoImpl.tickerId
                        data[tickerId]?.let { it ->
                            val articleList = it.map {
                                ArticleTitleQueryDtoImpl(it.first, it.second)
                            }.toList()
                            dtoImpl.articleTitles = articleList
                        }
                    }
                }

                "graph" -> {
                    val data = findGraphDataForPredictionWhenClosed()
                    results.forEach { dtoImpl ->
                        val tickerId = dtoImpl.tickerId
                        data[tickerId]?.let {
                            val graphData = PredictionListGraphDataQueryDtoImpl(false, it)
                            dtoImpl.graphData = graphData
                        }
                    }
                }

                else -> CriticalDataPollutedException("지원하지 않는 파라미터 타입입니다.")
            }
        }
    }

    /**
     * key: tickerId, value: List<Pair<title, articleId>>
     */
    private fun findArticleTitlesForPrediction(): Map<UUID, List<Pair<UUID, String>>> {
        val result = ArticleTickerTable.select(
            ArticleTickerTable.title,
            ArticleTickerTable.articleId,
            ArticleTickerTable.tickerId
        ).where {
            ArticleTickerTable.publishedDate.date() eq LocalDateTime.now().toLocalDate()
        }

        return result.groupBy(
            keySelector = { row ->
                row[ArticleTickerTable.tickerId]
            },
            // Value: Pair(ArticleId, Title) 리스트
            valueTransform = { row ->
                row[ArticleTickerTable.articleId] to row[ArticleTickerTable.title]
            }
        )
    }

    /**
     * key: tickerId, value: List<BigDecimal>
     */
    // [TODO]: 장이 열렸울때 실시간 데이터 8개를 받아오는 쿼리 추가 구현(dynamoDBRepo 여기서 호출 혹은 impl에서 호출 방식 고민 필요)
    private fun findGraphDataForPredictionWhenClosed(): Map<UUID, List<BigDecimal>> {
        val startDate = LocalDate.now().minusDays(15)

        val result = TickerPriceTable.select(
            TickerPriceTable.tickerId,
            TickerPriceTable.close
        ).where { TickerPriceTable.priceDate.date() greaterEq startDate }
            .orderBy(TickerPriceTable.priceDate, SortOrder.DESC)

        return result.groupBy(
            keySelector = { row ->
                row[TickerPriceTable.tickerId]
            },
            valueTransform = { row ->
                row[TickerPriceTable.close]
            }
        )
    }

}
