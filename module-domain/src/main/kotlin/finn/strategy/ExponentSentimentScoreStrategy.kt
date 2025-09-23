package finn.strategy

import finn.task.ExponentPredictionUnitTask
import org.springframework.stereotype.Component

@Component
class ExponentSentimentScoreStrategy : SentimentScoreStrategy<ExponentPredictionUnitTask> {

    override fun supports(type: String): Boolean = type == "exponent"

    override suspend fun calculate(task: ExponentPredictionUnitTask): Int {
        TODO("Not yet implemented")
    }
}