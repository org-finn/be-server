package finn.mapper

import finn.entity.Prediction
import finn.paging.PageResponse
import finn.queryDto.NewsDataQueryDto
import finn.queryDto.PredictionDetailQueryDto
import finn.response.prediciton.PredictionDetailResponse
import finn.response.prediciton.PredictionListResponse


fun toDto(predictionList: PageResponse<Prediction>): PredictionListResponse {
    val predictionDate = predictionList.content.firstOrNull()?.predictionDate
    val predictionDtoList = predictionList.content
        .map { it ->
            PredictionListResponse.PredictionDataResponse(
                it.tickerId,
                it.shortCompanyName,
                it.tickerCode,
                it.predictionStrategy.strategy,
                it.sentiment,
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
    val prediction = predictionDetail.prediction()

    val news = newsDataList.map { it ->
        PredictionDetailResponse.PredictionDetailDataResponse.NewsDataResponse(
            it.newsId(),
            it.headline(),
            it.sentiment(),
            it.reasoning()
        )
    }.toList()

    val predictionDetailData = PredictionDetailResponse.PredictionDetailDataResponse(
        predictionDetail.priceDate().toString(),
        predictionDetail.open(),
        predictionDetail.close(),
        predictionDetail.high(),
        predictionDetail.low(),
        predictionDetail.volume(),
        news
    )
    return PredictionDetailResponse(
        prediction.predictionDate.toString(),
        prediction.tickerId,
        prediction.shortCompanyName,
        prediction.tickerCode,
        prediction.predictionStrategy.strategy,
        prediction.sentiment,
        prediction.getNewsCountAlongWithStrategy(),
        prediction.sentimentScore,
        predictionDetailData
    )
}