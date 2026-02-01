package finn.request.kis

import com.fasterxml.jackson.annotation.JsonProperty

data class KISRealTimeTickerPriceRequest(
    val header: ExecutionRequestHeader,
    val body: ExecutionRequestBody
) {
    data class ExecutionRequestHeader(
        @JsonProperty("approval_key")
        val approvalKey: String,

        @JsonProperty("custtype")
        val custtype: String = "P", // 개인

        @JsonProperty("tr_type")
        val trType: String = "1",   // 1: 등록, 2: 해제

        @JsonProperty("content-type")
        val contentType: String = "utf-8"
    )

    data class ExecutionRequestBody(
        val input: ExecutionRequestInput
    )

    data class ExecutionRequestInput(
        @JsonProperty("tr_id")
        val trId: String = "HDFSCNT0", // 해외주식 실시간 체결가 ID

        @JsonProperty("tr_key")
        val trKey: String  // 형식: D + 시장구분(NAS/NYS/AMS) + 종목코드 (예: DNASAAPL)
    )
}



