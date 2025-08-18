package finn.moduleApi.service

import finn.moduleApi.paging.PageResponse
import finn.moduleApi.paging.PredictionPageRequest
import finn.moduleApi.queryDto.PredictionDetailQueryDto
import finn.moduleApi.repository.PredictionRepository
import finn.moduleDomain.entity.Prediction
import org.springframework.stereotype.Service
import java.util.*

@Service
class PredictionQueryService(private val predictionRepository: PredictionRepository) {

    fun getPredictionList(pageRequest: PredictionPageRequest) : PageResponse<Prediction> {
        return predictionRepository.getPredictionList(pageRequest)
    }

    fun getPredictionDetail(tickerId: UUID) : PredictionDetailQueryDto {
        return predictionRepository.getPredictionDetail(tickerId)
    }
}