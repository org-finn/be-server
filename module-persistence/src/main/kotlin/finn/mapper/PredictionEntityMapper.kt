package finn.mapper

import finn.entity.Prediction
import finn.entity.PredictionExposed
import finn.entity.PredictionStrategy

fun toDomain(prediction: PredictionExposed): Prediction {
    return Prediction.createForFetch(
        prediction.tickerId,
        prediction.tickerCode,
        prediction.shortCompanyName,
        prediction.positiveNewsCount,
        prediction.negativeNewsCount,
        prediction.neutralNewsCount,
        prediction.score,
        PredictionStrategy.valueOf(prediction.strategy),
        prediction.sentiment,
        prediction.predictionDate
    )
}

