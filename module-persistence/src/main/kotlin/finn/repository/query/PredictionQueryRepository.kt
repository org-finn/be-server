package finn.repository.query

import finn.entity.PredictionExposed
import finn.exception.CriticalDataOmittedException
import finn.insertDto.PredictionToInsert
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import finn.table.PredictionTable
import finn.table.TickerPriceTable
import finn.table.TickerTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
class PredictionQueryRepository {

    private data class PredictionQueryDtoImpl(
        val predictionDate: LocalDateTime,
        val tickerId: UUID,
        val shortCompanyName: String,
        val tickerCode: String,
        val predictionStrategy: String,
        val sentiment: Int,
        val articleCount: Long
    ) : PredictionQueryDto {
        override fun predictionDate(): LocalDateTime = this.predictionDate
        override fun tickerId(): UUID = this.tickerId
        override fun shortCompanyName(): String = this.shortCompanyName
        override fun tickerCode(): String = this.tickerCode
        override fun predictionStrategy(): String = this.predictionStrategy
        override fun sentiment(): Int = this.sentiment
        override fun articleCount(): Long = this.articleCount
    }

    fun findALlPredictionByPopular(page: Int, size: Int): PageResponse<PredictionQueryDto> {

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
            .orderBy(TickerTable.marketCap to SortOrder.DESC)
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
                articleCount = articleCount
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

    fun findALlPredictionBySentimentScore(
        page: Int,
        size: Int,
        isDownward: Boolean
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
            .orderBy(PredictionTable.score to sortOrder)
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
                articleCount = articleCount
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
        val maxDateExpression = PredictionTable.predictionDate.max()
        val latestDate = PredictionTable
            .select(maxDateExpression)
            .firstOrNull()
            ?.get(maxDateExpression)
            ?: throw CriticalDataOmittedException("치명적 오류: 주가 정보가 존재하지 않습니다.")

        return PredictionTable
            .join(
                TickerPriceTable, JoinType.INNER,
                PredictionTable.tickerId eq TickerTable.id
            )
            .select(
                PredictionTable.columns as Expression<*>,
                TickerPriceTable.priceDate,
                TickerPriceTable.open,
                TickerPriceTable.close,
                TickerPriceTable.high,
                TickerPriceTable.low,
                TickerPriceTable.volume
            )
            .where {
                (PredictionTable.predictionDate eq latestDate) and
                        (PredictionTable.tickerId eq tickerId)
            }.map { row ->
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
                    priceDate = row[TickerPriceTable.priceDate],
                    open = row[TickerPriceTable.open],
                    close = row[TickerPriceTable.close],
                    high = row[TickerPriceTable.high],
                    low = row[TickerPriceTable.low],
                    volume = row[TickerPriceTable.volume]
                )
            }.singleOrNull()
            ?: throw CriticalDataOmittedException("치명적 오류: ${tickerId}에 대한 예측 상세 정보가 존재하지 않습니다.")
    }

    fun save(prediction: PredictionToInsert) {
        PredictionExposed.new {
            predictionDate = prediction.predictionDate
            positiveArticleCount = prediction.positiveArticleCount
            negativeArticleCount = prediction.negativeArticleCount
            neutralArticleCount = prediction.neutralArticleCount
            sentiment = prediction.sentiment
            strategy = prediction.strategy
            score = prediction.sentimentScore
            tickerCode = prediction.tickerCode
            shortCompanyName = prediction.shortCompanyName
            tickerId = prediction.tickerId
            createdAt = LocalDateTime.now()
        }
    }
}