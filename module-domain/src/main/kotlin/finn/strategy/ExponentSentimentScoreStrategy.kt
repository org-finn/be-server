package finn.strategy

import finn.task.ExponentPredictionTask
import org.springframework.stereotype.Component

@Component
class ExponentSentimentScoreStrategy : SentimentScoreStrategy<ExponentPredictionTask> {

    override fun supports(type: String): Boolean = type == "exponent"

    override suspend fun calculate(task: ExponentPredictionTask): Int {
        TODO("Not yet implemented")
    }
}