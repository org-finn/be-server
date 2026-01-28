package finn.mapper

import finn.queryDto.FavoriteTickerQueryDto
import finn.response.userinfo.FavoriteTickerResponse

class FavoriteTIckerDtoMapper {
    companion object {

        fun toDto(tickers: List<FavoriteTickerQueryDto>): FavoriteTickerResponse {
            return FavoriteTickerResponse(tickers.map {
                FavoriteTickerResponse.FavoriteTicker(it.tickerId, it.tickerCode)
            }.toList())
        }
    }
}