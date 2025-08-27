package finn.service

import finn.entity.Article
import finn.repository.PredictionRepository
import org.springframework.stereotype.Service

@Service
class PredictionCommandService(
    private val predictionRepository: PredictionRepository
) {

    fun savePrediction(articleList: List<Article>) {

    }
}