package finn.mapper

import finn.queryDto.FavoriteTickerQueryDto
import finn.response.prediciton.PredictionListResponse.PredictionListGraphDataResponse
import finn.response.userinfo.FavoriteTickerResponse

class FavoriteTIckerDtoMapper {
    companion object {

        fun toDto(tickers: List<FavoriteTickerQueryDto>): FavoriteTickerResponse {
            return FavoriteTickerResponse(tickers.map { it ->
                FavoriteTickerResponse.FavoriteTicker(
                    it.tickerId,
                    it.tickerCode,
                    it.shortCompanyName,
                    it.predictionStrategy,
                    it.sentiment,
                    it.graphData?.let { it2 ->
                        PredictionListGraphDataResponse(
                            it2.marketOpen,
                            it2.priceData
                        )
                    }
                )
            }.toList())
        }
    }
}