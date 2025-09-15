package finn.mapper

import finn.entity.PredictionExposed
import finn.entity.query.PredictionQ


fun toDomain(prediction: PredictionExposed): PredictionQ {
    return PredictionQ.create(
        prediction.tickerId,
        prediction.tickerCode,
        prediction.shortCompanyName,
        prediction.positiveArticleCount,
        prediction.negativeArticleCount,
        prediction.neutralArticleCount,
        prediction.score,
        prediction.sentiment,
        prediction.strategy,
        prediction.predictionDate
    )
}