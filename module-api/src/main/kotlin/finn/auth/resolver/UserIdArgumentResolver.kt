package finn.config.resolver

import finn.auth.OptionalAuth
import finn.auth.UserId
import finn.exception.auth.InvalidTokenException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.*

@Component
class UserIdArgumentResolver : HandlerMethodArgumentResolver {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        // @UserId가 붙어있고, 타입이 UUID 이거나 UUID? 인 경우
        return parameter.hasParameterAnnotation(UserId::class.java) &&
                (parameter.parameterType == UUID::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): UUID? {

        // 1. Request에서 userId 꺼내기
        val userId = webRequest.getAttribute("userId", RequestAttributes.SCOPE_REQUEST) as UUID?

        // 2. userId가 존재하면 바로 반환 (로그인 상태)
        if (userId != null) {
            return userId
        }

        // 3. userId가 없을 때 (비로그인 상태) 처리
        // 해당 컨트롤러 메서드에 @OptionalAuth가 붙어있는지 확인
        val isOptional = parameter.method?.isAnnotationPresent(OptionalAuth::class.java) == true

        // 혹은 Kotlin의 Nullable 타입(UUID?)인지 확인
        val isNullable = parameter.isOptional

        if (isOptional || isNullable) {
            return null // 게스트로 인정
        }

        // 필수인데 없으면 예외 발생
        log.error { "액세스 토큰 내에 subject를 추출하는데 실패했습니다. 변조되었거나 유효하지 않은 토큰일 가능성이 있습니다." }
        throw InvalidTokenException("만료되었거나 유효하지 않은 토큰입니다.")
    }
}