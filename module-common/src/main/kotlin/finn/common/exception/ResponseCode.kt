package finn.common.exception

enum class ResponseCode(val code: String, val defaultMessage: String) {
    // 200 OK
    OK("200 OK", "요청을 성공적으로 처리하였습니다."),

    // 201 CREATED
    CREATED("201 CREATED", "리소스가 생성되었습니다."),

    // 204 NO CONTENT
    NO_CONTENT("204 No Content", "반환할 값이 없습니다."),

    // 400 BAD_REQUEST
    BAD_REQUEST("400 BAD_REQUEST", "잘못된 인자 값입니다."),

    // 401 UNAUTHORIZED
    UNAUTHORIZED("401 UNAUTHORIZED", "인증에 실패하였습니다."),

    // 404 NOT_FOUND
    NOT_FOUND("404 NOT_FOUND", "해당 사용자를 찾을 수 없습니다."),

    // 409 CONFLICT
    CONFLICT("409 CONFLICT", "이미 사용 중인 값입니다."),

    // 500 INTERNAL_SERVER_ERROR
    SERVER_ERROR("500 INTERNAL SERVER ERROR", "서버 내부 오류가 발생했습니다."),

    // 503 SERVICE_UNAVAILABLE
    SERVICE_UNAVAILABLE(
        "503 SERVICE_UNAVAILABLE",
        "서버가 일시적으로 동작하지 않습니다. 잠시 뒤 다시 시도해주세요."
    )
}