package finn.exception

import finn.response.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException


@RestControllerAdvice
class GlobalExceptionHandler() {
    companion object {
        private val log = KotlinLogging.logger {}
    }


    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handle400Ex(e: CommonException): ErrorResponse {
        printException(e)
        val responseCode = e.code
        val message = e.message ?: responseCode.defaultMessage
        val errorResponse = ErrorResponse(responseCode.code, message)

        return errorResponse
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handle404Ex(e: CommonException): ErrorResponse {
        printException(e)
        val responseCode = e.code
        val message = e.message ?: responseCode.defaultMessage
        val errorResponse = ErrorResponse(responseCode.code, message)

        return errorResponse
    }

    @ExceptionHandler(ServerErrorException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handle500Ex(e: CommonException): ErrorResponse {
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

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<Any> {
        // ERROR 로그를 남기지 않고 404 응답만 반환 (경보 필터링 위함)
        return ResponseEntity.notFound().build()
    }

    private fun printException(e: Exception) {
        val origin = e.stackTrace[0]
        log.error(e) {
            "[예외 발생] 클래스: ${origin.className} / 메서드: ${origin.methodName}\n[메시지] ${e.message}\n[스택-트레이스]"
        }
    }
}