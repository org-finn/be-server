package finn.moduleApi.exception

import finn.moduleApi.response.ResponseCode
import org.springframework.stereotype.Component

@Component
object ExceptionResponseCodeMapper {
    fun mapResponseCode(e: Exception): ResponseCode {
        val className = e::class.simpleName ?: ResponseCode.SERVER_ERROR.toString()

        return ResponseCode.entries.firstOrNull { code ->
            val keyword = code.name.replace("_", "") // 언더스코어 표기 무시
            className.contains(keyword, true)
        } ?: ResponseCode.SERVER_ERROR
    }
}