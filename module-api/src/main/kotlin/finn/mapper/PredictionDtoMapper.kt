package finn.mapper

import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
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
                it.articleCount()
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
    ArticleDataList: List<ArticleDataQueryDto>
): PredictionDetailResponse {
    val Article = ArticleDataList.map { it ->
        PredictionDetailResponse.PredictionDetailDataResponse.ArticleDataResponse(
            it.articleId(),
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
        Article
    )
    return PredictionDetailResponse(
        predictionDate = predictionDetail.predictionDate().toString(),
        tickerId = predictionDetail.tickerId(),
        shortCompanyName = predictionDetail.shortCompanyName(),
        tickerCode = predictionDetail.tickerCode(),
        predictionStrategy = predictionDetail.predictionStrategy(),
        sentiment = predictionDetail.sentiment(),
        articleCount = predictionDetail.articleCount(),
        sentimentScore = predictionDetail.sentimentScore(),
        detailData = predictionDetailData
    )
}