package finn.handler.k6

import finn.exception.NotSupportedTypeException
import org.springframework.stereotype.Component

@Component
class K6SimulationHandlerFactory(
    private val handlers: List<K6SimulationPredictionHandler>
) {
    fun findHandler(type: String): K6SimulationPredictionHandler {
        return handlers.find { it.supports(type) }
            ?: throw NotSupportedTypeException("지원하지 않는 예측 타입입니다: $type")
    }
}