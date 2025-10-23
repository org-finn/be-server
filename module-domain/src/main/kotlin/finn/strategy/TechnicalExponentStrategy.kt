package finn.strategy

interface TechnicalExponentStrategy<P> : ArtickerStrategy<P> {
    override suspend fun calculate(task: P) : Any
}