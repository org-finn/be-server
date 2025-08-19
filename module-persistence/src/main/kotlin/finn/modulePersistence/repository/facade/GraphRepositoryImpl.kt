package finn.modulePersistence.repository.facade

import finn.moduleDomain.queryDto.TickerGraphQueryDto
import finn.moduleDomain.repository.GraphRepository
import finn.modulePersistence.repository.db.GraphExposedRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class GraphRepositoryImpl(
    private val graphExposedRepository: GraphExposedRepository
) : GraphRepository {
    override fun getTickerGraph(
        tickerId: UUID,
        period: String
    ): List<TickerGraphQueryDto> {
        TODO("Not yet implemented")
    }
}