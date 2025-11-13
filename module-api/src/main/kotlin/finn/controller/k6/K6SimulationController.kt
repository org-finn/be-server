package finn.controller.k6

import finn.orchestrator.k6.BlockingOrchestrator
import finn.orchestrator.k6.NonBlockingOrchestrator
import finn.task.PredictionTask
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class K6SimulationController(
    private val blockingOrchestrator: BlockingOrchestrator,
    private val nonBlockingOrchestrator: NonBlockingOrchestrator
) {

    @PostMapping("/test/coroutine")
    suspend fun simulateCoroutine(@RequestBody task: PredictionTask) {
        nonBlockingOrchestrator.simulate(task)
    }

    @PostMapping("/test/blocking")
    fun simulateBlocking(@RequestBody task: PredictionTask) {
        blockingOrchestrator.simulate(task)
    }
}