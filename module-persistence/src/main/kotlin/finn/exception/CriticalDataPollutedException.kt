package finn.exception

/*
    지원하지 않는 옵션이 유입되어 오염
 */
class CriticalDataPollutedException(message: String, cause: Throwable?) :
    ServerErrorException(message, cause) {

    constructor(message: String) : this(message, null)
}