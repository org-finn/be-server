package finn.moduleDomain.entity

import java.time.LocalDateTime
import java.util.*

class Prediction(
    val tickerId: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val predictionStrategy: PredictionStrategy,
    val sentiment : Int,
    val newsCount : Int,
    val sentimentScore: Int,
    val predictionDate: LocalDateTime
) {


}