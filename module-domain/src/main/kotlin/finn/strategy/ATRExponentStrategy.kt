package finn.strategy

import finn.task.InitPredictionTask
import org.springframework.stereotype.Component

@Component
class ATRExponentStrategy : TechnicalExponentStrategy<InitPredictionTask> {
    override fun supports(type: String): Boolean = type == "init"

    override suspend fun calculate(task: InitPredictionTask): Int {
        TODO("Not yet implemented")
    }
}