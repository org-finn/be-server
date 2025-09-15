package finn.queryDto

import java.time.LocalDateTime
import java.util.*

interface PredictionQueryDto {
    fun predictionDate(): LocalDateTime

    fun tickerId(): UUID

    fun shortCompanyName(): String

    fun tickerCode(): String

    fun predictionStrategy(): String

    fun sentiment(): Int

    fun articleCount(): Long
}