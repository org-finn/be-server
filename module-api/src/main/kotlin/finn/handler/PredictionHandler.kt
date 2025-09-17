package finn.handler

import finn.score.task.PredictionTask

interface PredictionHandler {
    fun supports(type: String): Boolean
    suspend fun handle(task: PredictionTask)
}