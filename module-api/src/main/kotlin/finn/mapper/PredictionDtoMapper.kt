package finn.mapper

import finn.paging.PageResponse
import finn.queryDto.NewsDataQueryDto
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import finn.response.prediciton.PredictionDetailResponse
import finn.response.prediciton.PredictionListResponse


fun toDto(predictionList: PageResponse<PredictionQueryDto>): PredictionListResponse {
    val predictionDate = predictionList.content.firstOrNull()?.predictionDate()

    val predictionDtoList = predictionList.content
        .map { it ->
            // 2. DTO의 각 필드를 인터페이스 메서드 호출로 변경합니다.
            PredictionListResponse.PredictionDataResponse(
                it.tickerId(),
                it.shortCompanyName(),
                it.tickerCode(),
                it.predictionStrategy(),
                it.sentiment(),
                it.newsCount()
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
        predictionDate = predictionDetail.predictionDate().toString(),
        tickerId = predictionDetail.tickerId(),
        shortCompanyName = predictionDetail.shortCompanyName(),
        tickerCode = predictionDetail.tickerCode(),
        predictionStrategy = predictionDetail.predictionStrategy(),
        sentiment = predictionDetail.sentiment(),
        newsCount = predictionDetail.newsCount(),
        sentimentScore = predictionDetail.sentimentScore(),
        detailData = predictionDetailData
    )
}