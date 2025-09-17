package finn.controller

import finn.apiSpec.LambdaApiSpec
import finn.orchestrator.LambdaOrchestrator
import finn.request.lambda.LambdaArticleRealTimeRequest
import finn.response.SuccessResponse
import finn.score.task.PredictionTask
import org.springframework.web.bind.annotation.RestController

@RestController
class LambdaController(
    private val lambdaOrchestrator: LambdaOrchestrator
) : LambdaApiSpec {
    override fun processArticleRealTime(articleRequest: LambdaArticleRealTimeRequest) {
        lambdaOrchestrator.saveArticle(articleRequest)
    }

    override fun processPrediction(task: PredictionTask): SuccessResponse<Unit> {
        lambdaOrchestrator.updatePrediction(task)
        return SuccessResponse("200 OK", "예측 요청을 성공적으로 요청하였습니다.", null)
    }
}