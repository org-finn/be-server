package finn.exception

import finn.response.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class GlobalExceptionHandler() {
    companion object {
        private val log = KotlinLogging.logger {}
    }


    @ExceptionHandler(CommonException::class)
    @ResponseStatus(HttpStatus.OK)
    fun handleCommonEx(e: CommonException): ErrorResponse {
        printException(e)
        val responseCode = e.code
        val message = e.message ?: responseCode.defaultMessage
        val errorResponse = ErrorResponse(responseCode.code, message)

        return errorResponse
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleEx(e: Exception): ErrorResponse {
        printException(e)
        val responseCode = ResponseCode.SERVER_ERROR
        val errorResponse = ErrorResponse(responseCode.code, responseCode.defaultMessage)

        return errorResponse
    }

    private fun printException(e: Exception) {
        val origin = e.stackTrace[0]
        log.error(
            "[예외 발생] 클래스: {} / 메서드: {}\n[메시지] {}\n[스택-트레이스]",
            origin.className, origin.methodName, e.message, e
        )
    }
}