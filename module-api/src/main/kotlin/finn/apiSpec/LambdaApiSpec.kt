package finn.apiSpec

import finn.request.lambda.ArticleRealTimeRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/v1/lambda")
interface LambdaApiSpec {

    @PostMapping("/article/real-time")
    fun processArticleRealTime(@RequestBody articleRequest: ArticleRealTimeRequest)
}