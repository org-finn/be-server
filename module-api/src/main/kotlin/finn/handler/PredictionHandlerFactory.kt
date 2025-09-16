package finn.handler

import finn.exception.NotSupportedTypeException
import org.springframework.stereotype.Component

@Component
class PredictionHandlerFactory(
    private val handlers: List<PredictionHandler>
) {
    fun findHandler(type: String): PredictionHandler {
        return handlers.find { it.supports(type) }
            ?: throw NotSupportedTypeException("지원하지 않는 예측 타입입니다: $type")
    }
}