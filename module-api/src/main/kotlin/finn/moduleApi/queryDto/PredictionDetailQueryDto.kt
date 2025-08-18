package finn.moduleApi.queryDto

import finn.moduleDomain.entity.Prediction
import java.math.BigDecimal
import java.time.LocalDateTime

interface PredictionDetailQueryDto {
    fun getPrediction(): Prediction

    fun getPriceDate(): LocalDateTime

    fun getOpen(): BigDecimal

    fun getClose(): BigDecimal

    fun getHigh(): BigDecimal

    fun getLow(): BigDecimal

    fun getVolume(): Long

}