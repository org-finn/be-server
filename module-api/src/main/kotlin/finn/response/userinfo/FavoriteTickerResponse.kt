package finn.response.userinfo

import java.util.*

data class FavoriteTickerResponse(
    val tickers: List<FavoriteTicker>
) {
    data class FavoriteTicker(
        val tickerId: UUID,
        val tickerCode: String
    )
}
