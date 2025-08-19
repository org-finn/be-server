package finn.modulePersistence.repository.facade

import finn.moduleDomain.entity.Prediction
import finn.moduleDomain.paging.PageResponse
import finn.moduleDomain.queryDto.PredictionDetailQueryDto
import finn.moduleDomain.repository.PredictionRepository
import finn.modulePersistence.repository.db.PredictionExposedRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PredictionRepositoryImpl(
    private val predictionExposedRepository: PredictionExposedRepository
) : PredictionRepository {
    override fun getPredictionList(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<Prediction> {
        TODO("Not yet implemented")
    }

    override fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto {
        TODO("Not yet implemented")
    }
}