package finn.handler

import finn.task.PredictionTask

interface PredictionHandler {
    fun supports(type: String): Boolean
    suspend fun handle(task: PredictionTask)
}