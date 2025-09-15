package finn.service

import finn.paging.PageResponse
import finn.paging.PredictionPageRequest
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import finn.repository.PredictionRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class PredictionQueryService(private val predictionRepository: PredictionRepository) {

    fun getPredictionList(pageRequest: PredictionPageRequest): PageResponse<PredictionQueryDto> {
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