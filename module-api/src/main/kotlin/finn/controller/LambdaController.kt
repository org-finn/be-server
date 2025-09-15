package finn.controller

import finn.apiSpec.LambdaApiSpec
import finn.orchestrator.LambdaOrchestrator
import finn.request.lambda.LambdaArticleRealTimeRequest
import finn.request.lambda.LambdaPredictionRequest
import org.springframework.web.bind.annotation.RestController

@RestController
class LambdaController(
    private val lambdaOrchestrator: LambdaOrchestrator
) : LambdaApiSpec {
    override fun processArticleRealTime(articleRequest: LambdaArticleRealTimeRequest) {
        lambdaOrchestrator.saveArticle(articleRequest)
    }

    override fun processPrediction(predictionRequest: LambdaPredictionRequest) {
        lambdaOrchestrator.savePrediction(predictionRequest)
    }
}