package finn.moduleApi.service

import finn.moduleApi.paging.PredictionPageRequest
import finn.moduleDomain.entity.Prediction
import finn.moduleDomain.paging.PageResponse
import finn.moduleDomain.queryDto.PredictionDetailQueryDto
import finn.moduleDomain.repository.PredictionRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class PredictionQueryService(private val predictionRepository: PredictionRepository) {

    fun getPredictionList(pageRequest: PredictionPageRequest): PageResponse<Prediction> {
        return predictionRepository.getPredictionList(
            pageRequest.page,
            pageRequest.size,
            pageRequest.sort
        )
    }

    fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto {
        return predictionRepository.getPredictionDetail(tickerId)
    }
}