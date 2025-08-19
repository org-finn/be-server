package finn.entity

import java.util.*

class Ticker private constructor(
    val id: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val fullCompanyName: String
) {
    companion object {
        fun create(
            id: UUID,
            tickerCode: String,
            shortCompanyName: String,
            fullCompanyName: String
        ): Ticker {
            return Ticker(id, tickerCode, shortCompanyName, fullCompanyName)
        }
    }
}
