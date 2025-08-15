package finn.api.exception

import finn.api.response.ResponseCode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

internal class ExceptionResponseCodeMapperTest : BehaviorSpec({

    val mapper = ExceptionResponseCodeMapper

    // 테스트에 사용할 커스텀 예외 클래스들
    class NotFoundUserException : Exception()
    class UserConflictException : Exception()
    class badRequestParameterException : Exception()
    class CustomDomainException : Exception()

    Given("다양한 종류의 예외가 주어졌을 때") {
        withData(
            // 테스트 케이스의 이름을 동적으로 생성
            nameFn = { (exception, _) -> "예외 타입: ${exception::class.simpleName}" },

            // 테스트 데이터 목록 (입력값 to 기대값)
            NotFoundUserException() to ResponseCode.NOT_FOUND,
            UserConflictException() to ResponseCode.CONFLICT,
            badRequestParameterException() to ResponseCode.BAD_REQUEST,
            CustomDomainException() to ResponseCode.INTERNAL_SERVER_ERROR,
            IllegalArgumentException() to ResponseCode.INTERNAL_SERVER_ERROR,
            object : Exception() {} to ResponseCode.INTERNAL_SERVER_ERROR
        ) { (exception, expectedCode) -> // Pair를 구조 분해하여 사용
            When("Mapper를 통해 ResponseCode로 변환하면") {
                val actualCode = mapper.mapResponseCode(exception)
                Then("예상하는 ResponseCode가 반환되어야 한다") {
                    actualCode shouldBe expectedCode
                }
            }
        }
    }

})