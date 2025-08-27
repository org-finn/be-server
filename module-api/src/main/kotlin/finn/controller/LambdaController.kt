package finn.controller

import finn.apiSpec.LambdaApiSpec
import finn.orchestrator.LambdaOrchestrator
import finn.request.lambda.ArticleRealTimeBatchRequest
import org.springframework.web.bind.annotation.RestController

@RestController
class LambdaController(
    private val lambdaOrchestrator: LambdaOrchestrator
) : LambdaApiSpec {
    override fun processArticleRealTime(articleBatchRequest: ArticleRealTimeBatchRequest) {
        lambdaOrchestrator.saveArticleAndPrediction(articleBatchRequest)
    }

}