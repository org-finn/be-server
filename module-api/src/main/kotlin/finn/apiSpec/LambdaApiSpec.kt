package finn.apiSpec

import finn.request.lambda.LambdaArticleRealTimeRequest
import finn.request.lambda.LambdaPredictionRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/lambda/v1")
interface LambdaApiSpec {

    @PostMapping("/article/real-time")
    fun processArticleRealTime(@RequestBody articleRequest: LambdaArticleRealTimeRequest)

    @PostMapping("/prediction")
    fun processPrediction(@RequestBody predictionRequest: LambdaPredictionRequest)
}