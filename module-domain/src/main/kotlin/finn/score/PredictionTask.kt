package finn.score

import java.util.*

data class PredictionTask(
    val tickerId: UUID,
    val type: String,
    val payload: MutableMap<String, Any>
)
