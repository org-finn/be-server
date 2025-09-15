package finn.queryDto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface PredictionDetailQueryDto {
    fun predictionDate(): LocalDateTime

    fun tickerId(): UUID

    fun shortCompanyName(): String

    fun tickerCode(): String

    fun predictionStrategy(): String

    fun sentiment(): Int

    fun articleCount(): Long

    fun sentimentScore(): Int

    fun priceDate(): LocalDate

    fun open(): BigDecimal

    fun close(): BigDecimal

    fun high(): BigDecimal

    fun low(): BigDecimal

    fun volume(): Long

}