package finn.config.resolver

import finn.exception.auth.InvalidTokenException
import finn.resolver.UserId
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
        val hasAnnotation = parameter.hasParameterAnnotation(UserId::class.java)
        val isUuidType = UUID::class.java.isAssignableFrom(parameter.parameterType)
        return hasAnnotation && isUuidType
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): UUID {
        val userId = webRequest.getAttribute("userId", RequestAttributes.SCOPE_REQUEST)
            ?: {
                log.error { "액세스 토큰 내에 userId subject가 누락되었습니다. 올바른 토큰인지 확인이 필요합니다." }
                throw InvalidTokenException("만료되었거나 유효하지 않은 토큰입니다.")
            }

        return userId as UUID
    }
}