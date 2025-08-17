package finn.moduleDomain.exception

import finn.moduleCommon.exception.CommonException

class ServerErrorCriticalDataOmittedException(message: String, cause: Throwable?) :
    CommonException(message, cause) {
    constructor(message: String) : this(message, null)
}