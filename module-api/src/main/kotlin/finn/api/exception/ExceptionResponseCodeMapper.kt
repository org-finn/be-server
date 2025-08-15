package finn.api.exception

import finn.api.response.ResponseCode
import org.springframework.stereotype.Component

@Component
object ExceptionResponseCodeMapper {
    fun mapResponseCode(e: Exception): ResponseCode {
        val className = e::class.simpleName ?: ResponseCode.INTERNAL_SERVER_ERROR.toString()

        return ResponseCode.entries.firstOrNull { code ->
            className.contains(code.name, true)
        } ?: ResponseCode.INTERNAL_SERVER_ERROR
    }
}