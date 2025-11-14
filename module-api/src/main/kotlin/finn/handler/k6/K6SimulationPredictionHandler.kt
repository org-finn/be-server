package finn.handler.k6

import finn.task.PredictionTask

interface K6SimulationPredictionHandler {
    fun supports(type: String): Boolean

    fun handleBlocking(task: PredictionTask)

    suspend fun handleNonBlocking(task: PredictionTask)
}