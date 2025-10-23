package finn.strategy

interface ArtickerStrategy<P> {
    // 자신의 타입과 일치하는지 확인
    fun supports(type: String): Boolean
    // 실제 계산 로직 (suspend 함수)
    suspend fun calculate(task: P) : Any
}