package finn.exception

class ServerErrorCriticalDataOmittedException(message: String, cause: Throwable?) :
    CommonException(message, cause) {
    constructor(message: String) : this(message, null)
}