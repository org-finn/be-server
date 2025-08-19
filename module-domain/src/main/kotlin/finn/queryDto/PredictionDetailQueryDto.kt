package finn.queryDto

import finn.entity.Prediction
import java.math.BigDecimal
import java.time.LocalDate

interface PredictionDetailQueryDto {
    fun prediction(): Prediction

    fun priceDate(): LocalDate

    fun open(): BigDecimal

    fun close(): BigDecimal

    fun high(): BigDecimal

    fun low(): BigDecimal

    fun volume(): Long

}