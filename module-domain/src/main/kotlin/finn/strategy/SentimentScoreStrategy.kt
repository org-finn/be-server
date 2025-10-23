package finn.strategy

interface SentimentScoreStrategy<P> : ArtickerStrategy<P> {
    override suspend fun calculate(task: P) : Int
}