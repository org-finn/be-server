package finn.controller

import finn.apiSpec.JoinApiSpec
import finn.orchestrator.JoinOrchestrator
import finn.response.SuccessResponse
import finn.response.userinfo.JoinTickerResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class JoinController(
    private val joinOrchestrator: JoinOrchestrator
) : JoinApiSpec {

    override fun getTickerList(page: Int): SuccessResponse<JoinTickerResponse> {
        val response = joinOrchestrator.getTickerList(page)
        return SuccessResponse("200 Ok", "종목 리스트 조회 성공", response)
    }
}