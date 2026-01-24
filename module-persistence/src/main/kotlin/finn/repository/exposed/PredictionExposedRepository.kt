package finn.repository.exposed

import finn.entity.PredictionExposed
import finn.entity.TickerScore
import finn.exception.CriticalDataOmittedException
import finn.exception.CriticalDataPollutedException
import finn.exception.NotFoundDataException
import finn.paging.PageResponse
import finn.queryDto.*
import finn.table.ArticleTickerTable
import finn.table.PredictionTable
import finn.table.TickerPriceTable
import finn.table.TickerTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

@Repository
class PredictionExposedRepository(
    private val clock: Clock,
) {
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

    fun batchInsertPredictions(predictions: List<PredictionCreateDto>) {
        PredictionTable.batchInsert(predictions) { dto ->
            this[PredictionTable.tickerId] = dto.tickerId
            this[PredictionTable.predictionDate] = dto.predictionDate
            this[PredictionTable.tickerCode] = dto.tickerCode
            this[PredictionTable.shortCompanyName] = dto.shortCompanyName
            this[PredictionTable.score] = dto.score
            this[PredictionTable.volatility] = dto.volatility
            this[PredictionTable.positiveArticleCount] = dto.positiveCount
            this[PredictionTable.negativeArticleCount] = dto.negativeCount
            this[PredictionTable.neutralArticleCount] = dto.neutralCount
            this[PredictionTable.sentiment] = dto.sentiment
            this[PredictionTable.strategy] = dto.strategy.strategy
            this[PredictionTable.createdAt] = LocalDateTime.now(clock)
        }
    }

    fun findAllPrediction(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<PredictionQueryDto> {

        val predictionExposedList = when (sort) {
            "popular" -> findAllPredictionByPopular(
                page,
                size
            )

            "upward" -> findAllPredictionBySentimentScore(
                page,
                size,
                false
            )

            "downward" -> findAllPredictionBySentimentScore(
                page,
                size,
                true
            )

            "volatility" -> findAllPredictionByVolatility(
                page,
                size
            )

            else -> throw CriticalDataPollutedException("Sort: $sort, 지원하지 않는 옵션입니다.")
        }

        return predictionExposedList
    }

    private fun findAllPredictionByPopular(
        page: Int,
        size: Int,
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

            PredictionQueryDto(
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

        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(
            content = content,
            page = page,
            size = content.size,
            hasNext = hasNext
        )
    }


    private fun findAllPredictionBySentimentScore(
        page: Int,
        size: Int,
        isDownward: Boolean,
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

            PredictionQueryDto(
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

        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(
            content = content,
            page = page,
            size = content.size,
            hasNext = hasNext
        )
    }

    private fun findAllPredictionByVolatility(
        page: Int,
        size: Int,
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

            PredictionQueryDto(
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

        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(
            content = content,
            page = page,
            size = content.size,
            hasNext = hasNext
        )
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
                PredictionDetailQueryDto(
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
            }.firstOrNull()
            ?: throw NotFoundDataException("치명적 오류: ${tickerId}에 대한 예측 상세 정보가 존재하지 않습니다.")
    }

    suspend fun findAllByTickerIds(tickerIds: List<UUID>): List<PredictionExposed> {
        if (tickerIds.isEmpty()) return emptyList()

        // 데드락 방지를 위해 ID 정렬
        val sortedIds = tickerIds.sorted()

        return PredictionExposed.find {
            (PredictionTable.tickerId inList sortedIds) and
                    (PredictionTable.predictionDate eq LocalDateTime.now().toLocalDate()
                        .atStartOfDay())
        }
            .toList()
    }

    suspend fun findAll(): List<PredictionExposed> {
        return PredictionExposed.all()
            .toList()
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
        } ?: throw NotFoundDataException("금일 일자로 생성된 ${tickerId}의 Prediction이 존재하지 않습니다.")
    }

    fun batchUpdatePredictions(updates: List<PredictionUpdateDto>, alpha: Double) {
        if (updates.isEmpty()) return

        // 1. 데드락 방지 정렬
        val sortedUpdates = updates.sortedWith(
            compareBy<PredictionUpdateDto> { it.tickerId }.thenBy { it.predictionDate }
        )

        val oneMinusAlpha = 1.0 - alpha

        // 2. SQL 작성
        // RMA 적용을 DB 단으로 이동(새롭게 계산된 점수를 아예 뒤집어쓰는 방식이 아닌 일부 반영 방식으로 변경)
        val sql = """
        UPDATE predictions
        SET 
            score = GREATEST(0, LEAST(100, 
                ROUND(
                    CASE 
                        WHEN score = 0 THEN ? 
                        ELSE (score * $oneMinusAlpha) + (? * $alpha) 
                    END
                )::integer
            )),
            positive_article_count = positive_article_count + ?,
            negative_article_count = negative_article_count + ?,
            neutral_article_count = neutral_article_count + ?,
            sentiment = ?,
            strategy = ?
        WHERE ticker_id = ? AND prediction_date = ?
    """.trimIndent()

        // 3. JDBC Connection 추출 및 배치 실행
        val exposedConn = TransactionManager.current().connection

        // Exposed 래퍼를 벗겨내고 실제 JDBC Connection을 가져옵니다.
        val jdbcConn = (exposedConn as? JdbcConnectionImpl)?.connection
            ?: throw IllegalStateException("Current connection is not a JDBC connection")

        // Try-Finally로 자원 해제 보장
        var stmt: PreparedStatement? = null
        try {
            stmt = jdbcConn.prepareStatement(sql)

            sortedUpdates.forEach { dto ->
                var idx = 1

                // -- SET 절 --
                stmt.setDouble(idx++, dto.score.toDouble())     // 초기값용
                stmt.setDouble(idx++, dto.score.toDouble())     // EMA 계산용
                stmt.setLong(idx++, dto.positiveArticleCount)
                stmt.setLong(idx++, dto.negativeArticleCount)
                stmt.setLong(idx++, dto.neutralArticleCount)
                stmt.setInt(idx++, dto.sentiment)
                stmt.setString(idx++, dto.strategy)

                // -- WHERE 절 --
                stmt.setObject(idx++, dto.tickerId)
                stmt.setDate(idx++, Date.valueOf(dto.predictionDate.toLocalDate()))

                // 배치에 추가
                stmt.addBatch()
            }

            // 일괄 실행
            stmt.executeBatch()

        } finally {
            stmt?.close()
        }
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
        val today = LocalDate.now(clock)
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
        val today = LocalDate.now(clock)

        return PredictionTable
            .select(PredictionTable.score)
            .where {
                (PredictionTable.tickerId eq tickerId) and
                        (PredictionTable.predictionDate.date() eq today)
            }
            .map { row ->
                row[PredictionTable.score]
            }.firstOrNull()
            ?: throw NotFoundDataException("금일 일자로 생성된 ${tickerId}의 Prediction이 존재하지 않습니다.")
    }

    suspend fun findTodaySentimentScoreList(): List<TickerScore> {
        val today = LocalDate.now(clock)

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
            }.firstOrNull()
            ?: throw NotFoundDataException("${tickerId}의 전일 변동성 지표 값이 존재하지 않습니다.")
    }

    /**
     * key: tickerId, value: positiveKeywords, negativeKeywords
     * keywords의 기근 문제를 해결하기 위해, 키워드 별로 3일 이내 중 가장 최신 데이터를 가져온다.
     */
    private fun findArticleSummaryKeywordsForPrediction(): Map<UUID, List<String?>> {
        val sevenDaysAgo = ZonedDateTime.now(ZoneId.of("UTC"))
            .minusDays(2)
            .truncatedTo(ChronoUnit.DAYS) // 시/분/초를 0으로 절삭
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) // DB가 인식 가능한 포맷 문자열로 변환

        val query = """
            SELECT
                ticker_id,
                (ARRAY_AGG(positive_keywords ORDER BY summary_date DESC) 
                 FILTER (WHERE positive_keywords IS NOT NULL))[1] AS latest_pos,
                (ARRAY_AGG(negative_keywords ORDER BY summary_date DESC) 
                 FILTER (WHERE negative_keywords IS NOT NULL))[1] AS latest_neg
            FROM article_summary
            WHERE summary_date >= '$sevenDaysAgo'::timestamptz
            GROUP BY ticker_id;
        """

        val resultMap = mutableMapOf<UUID, List<String?>>()

        // Exposed 트랜잭션 내에서 Raw SQL 실행
        TransactionManager.current().exec(query) { rs: ResultSet ->
            while (rs.next()) {
                val tickerIdStr = rs.getString("ticker_id")
                val tickerId = UUID.fromString(tickerIdStr)

                val positiveKeywords = rs.getString("latest_pos")
                val negativeKeywords = rs.getString("latest_neg")

                resultMap[tickerId] = listOf(positiveKeywords, negativeKeywords)
            }
        }

        return resultMap
    }

    fun setPredictionDataForParam(
        param: String,
        results: List<PredictionQueryDto>
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
                                ArticleTitleQueryDto(it.first, it.second)
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
                            val graphData = PredictionListGraphDataQueryDto(false, it)
                            dtoImpl.graphData = graphData
                        }
                    }
                }

                else -> throw CriticalDataPollutedException("지원하지 않는 파라미터 타입입니다.")
            }
        }
    }

    /**
     * key: tickerId, value: List<Pair<title, articleId>>
     */
    private fun findArticleTitlesForPrediction(): Map<UUID, List<Pair<UUID, String>>> {
        val targetDate = Instant.now(clock).minus(1, ChronoUnit.DAYS)

        val result = ArticleTickerTable.select(
            ArticleTickerTable.title,
            ArticleTickerTable.titleKr,
            ArticleTickerTable.tickerId,
            ArticleTickerTable.articleId
        ).where {
            ArticleTickerTable.publishedDate greaterEq targetDate
        }

        return result.groupBy(
            keySelector = { row ->
                row[ArticleTickerTable.tickerId]
            },
            // Value: Pair(ArticleId, Title) 리스트
            valueTransform = { row ->
                row[ArticleTickerTable.articleId] to (row[ArticleTickerTable.titleKr]
                    ?: row[ArticleTickerTable.title]) // 안전 장치로 원문 타이틀 도입
            }
        )
    }

    /**
     * key: tickerId, value: List<BigDecimal>
     */
    private fun findGraphDataForPredictionWhenClosed(): Map<UUID, List<BigDecimal>> {
        val startDate = LocalDate.now().minusDays(15)

        val result = TickerPriceTable.select(
            TickerPriceTable.tickerId,
            TickerPriceTable.close
        ).where { TickerPriceTable.priceDate.date() greaterEq startDate }
            .orderBy(TickerPriceTable.priceDate, SortOrder.ASC)

        return result.groupBy(
            keySelector = { row ->
                row[TickerPriceTable.tickerId]
            },
            valueTransform = { row ->
                row[TickerPriceTable.close]
            }
        )
    }

    fun findYesterdayVolatilities(tickerIds: List<UUID>): Map<UUID, BigDecimal> {
        if (tickerIds.isEmpty()) return emptyMap()

        // "어제"의 기준 정의 (휴일 처리가 이미 되었다면 하루 전 날짜, 혹은 가장 최근 날짜)
        // 여기서는 단순하게 하루 전 00:00:00 기준 데이터를 조회한다고 가정
        val yesterday = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay()

        return PredictionTable.select(PredictionTable.tickerId, PredictionTable.volatility)
            .where {
                (PredictionTable.tickerId inList tickerIds) and
                        (PredictionTable.predictionDate eq yesterday)
            }
            .associate { row ->
                row[PredictionTable.tickerId] to row[PredictionTable.volatility]
            }
    }
}
