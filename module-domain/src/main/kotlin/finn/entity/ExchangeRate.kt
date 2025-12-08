package finn.entity

import java.math.BigDecimal
import java.time.LocalDate

class ExchangeRate private constructor(
    val date: LocalDate,
    val indexCode: String,
    val indexInfo: String,
    val value: BigDecimal,
    val changeRate: BigDecimal
) {
    companion object {
        fun create(
            date: LocalDate,
            indexCode: String,
            indexInfo: String,
            value: BigDecimal,
            changeRate: BigDecimal
        ): ExchangeRate {
            return ExchangeRate(date, indexCode, indexInfo, value, changeRate)
        }
    }
}