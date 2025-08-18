package finn.moduleApi.mapper

import finn.moduleApi.paging.PageResponse
import finn.moduleApi.queryDto.NewsDataQueryDto
import finn.moduleApi.queryDto.PredictionDetailQueryDto
import finn.moduleApi.response.prediciton.PredictionDetailResponse
import finn.moduleApi.response.prediciton.PredictionDetailResponse.PredictionDetailDataResponse
import finn.moduleApi.response.prediciton.PredictionDetailResponse.PredictionDetailDataResponse.NewsDataResponse
import finn.moduleApi.response.prediciton.PredictionListResponse
import finn.moduleDomain.entity.Prediction

object PredictionDtoMapper {

    fun toDto(predictionList: PageResponse<Prediction>): PredictionListResponse {
        val predictionDate = predictionList.content.firstOrNull()?.predictionDate
        val predictionDtoList = predictionList.content
            .map { it ->
                PredictionListResponse.PredictionDataResponse(
                    it.tickerId,
                    it.shortCompanyName,
                    it.tickerCode,
                    it.predictionStrategy.strategy,
                    it.getSentiment(),
                    it.getNewsCountAlongWithStrategy()
                )
            }.toList()
        return PredictionListResponse(
            predictionDate.toString(),
            predictionDtoList,
            predictionList.page,
            predictionList.hasNext
        )
    }

    fun toDto(
        predictionDetail: PredictionDetailQueryDto,
        newsDataList: List<NewsDataQueryDto>
    ): PredictionDetailResponse {
        val prediction = predictionDetail.getPrediction()
        val news = newsDataList.map { it ->
            NewsDataResponse(it.getNewsId(), it.getHeadline(), it.getSentiment(), it.getReasoning())
        }.toList()

        val predictionDetail = PredictionDetailDataResponse(
            predictionDetail.getPriceDate().toString(),
            predictionDetail.getOpen(),
            predictionDetail.getClose(),
            predictionDetail.getHigh(),
            predictionDetail.getLow(),
            predictionDetail.getVolume(),
            news
        )
        return PredictionDetailResponse(
            prediction.predictionDate.toString(),
            prediction.tickerId,
            prediction.shortCompanyName,
            prediction.tickerCode,
            prediction.predictionStrategy.strategy,
            prediction.getSentiment(),
            prediction.getNewsCountAlongWithStrategy(),
            prediction.sentimentScore,
            predictionDetail
        )
    }
}