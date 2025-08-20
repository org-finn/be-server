package finn.exception

/*
    반드시 필요한 데이터가 누락되어 치명적 오류가 발생
 */
class ServerErrorCriticalDataOmittedException(message: String, cause: Throwable?) :
    CommonException(message, cause) {
    constructor(message: String) : this(message, null)
}