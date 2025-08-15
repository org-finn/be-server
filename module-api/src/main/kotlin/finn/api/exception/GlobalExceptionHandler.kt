package finn.api.exception

import finn.api.response.ErrorResponse
import finn.api.response.ResponseCode
import finn.common.exception.CommonException
import finn.common.logger.LoggerCreator
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(private val exceptionResponseCodeMapper: ExceptionResponseCodeMapper) {
    companion object : LoggerCreator()

    @ExceptionHandler(CommonException::class)
    fun handleCommonEx(e: CommonException): ResponseEntity<ErrorResponse> {
        printException(e)
        val responseCode = exceptionResponseCodeMapper.mapResponseCode(e)
        val errorResponse = ErrorResponse(responseCode.code, responseCode.defaultMessage)

        return ResponseEntity.status(HttpStatus.OK)
            .body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleEx(e: Exception): ResponseEntity<ErrorResponse> {
        printException(e)
        val responseCode = ResponseCode.INTERNAL_SERVER_ERROR
        val errorResponse = ErrorResponse(responseCode.code, responseCode.defaultMessage)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }

    private fun printException(e: Exception) {
        val origin = e.stackTrace[0]
        log.error(
            "[예외 발생] 클래스: {} / 메서드: {}\n[메시지] {}\n[스택-트레이스]",
            origin.className, origin.methodName, e.message, e
        )
    }
}