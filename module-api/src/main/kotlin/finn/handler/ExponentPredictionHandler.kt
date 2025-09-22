package finn.handler

import finn.exception.NotSupportedTypeException
import finn.service.PredictionCommandService
import finn.strategy.ExponentSentimentScoreStrategy
import finn.strategy.StrategyFactory
import finn.task.ExponentPredictionTask
import finn.task.PredictionTask
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Component
import java.sql.Connection

@Component
class ExponentPredictionHandler(
    private val predictionService: PredictionCommandService,
    private val strategyFactory: StrategyFactory
) : PredictionHandler {

    override fun supports(type: String): Boolean = type == "exponent"

    override suspend fun handle(task: PredictionTask) {
        newSuspendedTransaction(
            context = Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        ) {
            if (task !is ExponentPredictionTask) {
                throw NotSupportedTypeException("Unsupported prediction task type in Exponent Prediction: ${task.type}")
            }

            val strategy = strategyFactory.findStrategy(task.type)
            if (strategy !is ExponentSentimentScoreStrategy) {
                throw NotSupportedTypeException("Unsupported prediction strategy in Exponent Prediction: ${strategy.javaClass}")
            }
            strategy.calculate(task)

            // Prediction update
        }
    }
}