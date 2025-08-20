package finn.repository.query

import finn.entity.Prediction
import finn.entity.PredictionExposed
import finn.exception.ServerErrorCriticalDataOmittedException
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.table.PredictionTable
import finn.table.TickerPriceTable
import finn.table.TickerTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Repository
class PredictionQueryRepository {

    fun findALlPredictionByPopular(page: Int, size: Int): PageResponse<PredictionExposed> {

        val maxDateExpression = PredictionTable.predictionDate.max()
        val latestDate = PredictionTable
            .select(maxDateExpression)
            .firstOrNull()
            ?.get(maxDateExpression)
            ?: throw ServerErrorCriticalDataOmittedException("치명적 오류: 주가 정보가 존재하지 않습니다.")

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

        val results = PredictionExposed.wrapRows(query).toList()
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
    ): PageResponse<PredictionExposed> {

        val maxDateExpression = PredictionTable.predictionDate.max()
        val latestDate = PredictionTable
            .select(maxDateExpression)
            .firstOrNull()
            ?.get(maxDateExpression)
            ?: throw ServerErrorCriticalDataOmittedException("치명적 오류: 주가 정보가 존재하지 않습니다.")

        val sortOrder = if (isDownward) SortOrder.DESC else SortOrder.ASC

        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val query = PredictionTable
            .selectAll()
            .where(PredictionTable.predictionDate eq latestDate)
            .orderBy(PredictionTable.score to sortOrder)
            .limit(n = itemsToFetch, offset = offset)

        val results = PredictionExposed.wrapRows(query).toList()
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
        val prediction: Prediction,
        val priceDate: LocalDate,
        val open: BigDecimal,
        val close: BigDecimal,
        val high: BigDecimal,
        val low: BigDecimal,
        val volume: Long
    ) : PredictionDetailQueryDto {
        override fun prediction(): Prediction = this.prediction
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
            ?: throw ServerErrorCriticalDataOmittedException("치명적 오류: 주가 정보가 존재하지 않습니다.")

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
                PredictionTable.predictionDate eq latestDate
                PredictionTable.tickerId eq tickerId
            }.map { row ->
                PredictionDetailQueryDtoImpl(
                    prediction = toDomain(PredictionExposed.wrapRow(row)),
                    priceDate = row[TickerPriceTable.priceDate],
                    open = row[TickerPriceTable.open],
                    close = row[TickerPriceTable.close],
                    high = row[TickerPriceTable.high],
                    low = row[TickerPriceTable.low],
                    volume = row[TickerPriceTable.volume]
                )
            }.singleOrNull()
            ?: throw ServerErrorCriticalDataOmittedException("치명적 오류: ${tickerId}에 대한 예측 상세 정보가 존재하지 않습니다.")
    }
}