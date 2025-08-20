package finn.exception

/*
    반드시 필요한 데이터가 누락되어 치명적 오류가 발생
 */
class CriticalDataOmittedException(message: String, cause: Throwable?) :
    CommonException(message, cause) {
    override val code: ResponseCode = ResponseCode.SERVER_ERROR

    constructor(message: String) : this(message, null)
}