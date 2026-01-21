package finn.exception

// 인증 관련 DB 로직 문제 발생
class AuthenticationCriticalProblemException(message: String, cause: Throwable?) :
    ServerErrorException(message, cause) {

    constructor(message: String) : this(message, null)
}