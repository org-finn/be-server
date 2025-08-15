package finn.api.exception

import finn.api.response.ResponseCode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

internal class ExceptionResponseCodeMapperTest {

    private val mapper = ExceptionResponseCodeMapper

    class NotFoundUserException : Exception()
    class UserConflictException : Exception()
    class badRequestParameterException : Exception() // 일부러 소문자로 시작하여 대소문자 무시 테스트
    class CustomDomainException : Exception()     // 매칭되지 않는 케이스

    companion object {
        @JvmStatic // JUnit이 이 메서드를 찾을 수 있도록 @JvmStatic을 붙여줍니다.
        fun provideExceptionAndExpectedCode(): Stream<Arguments> {
            return Stream.of(
                // 1. 성공 케이스
                Arguments.of(NotFoundUserException(), ResponseCode.NOT_FOUND),
                Arguments.of(UserConflictException(), ResponseCode.CONFLICT),
                Arguments.of(badRequestParameterException(), ResponseCode.BAD_REQUEST),

                // 2. 실패 (기본값) 케이스
                Arguments.of(CustomDomainException(), ResponseCode.INTERNAL_SERVER_ERROR),
                Arguments.of(IllegalArgumentException(), ResponseCode.INTERNAL_SERVER_ERROR),

                // 3. 엣지 케이스 (존재하지 않는 익명 클래스)
                Arguments.of(object : Exception() {}, ResponseCode.INTERNAL_SERVER_ERROR)
            )
        }
    }

    @ParameterizedTest
    @MethodSource("provideExceptionAndExpectedCode")
    fun mapped_by_exception_class_name(
        exception: Exception,
        expectedCode: ResponseCode
    ) {
        // when: 매퍼를 통해 예외를 ResponseCode로 변환
        val actualCode = mapper.mapResponseCode(exception)

        // then: 예상한 ResponseCode와 실제 변환된 코드가 일치하는지 검증
        assertEquals(expectedCode, actualCode)
    }
}