package finn.task

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import finn.entity.TickerScore
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*
import kotlin.properties.Delegates

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,       // 타입을 식별하기 위해 이름을 사용
    include = JsonTypeInfo.As.PROPERTY, // JSON 프로퍼티(필드)를 보고 타입을 결정
    property = "type"                 // 그 프로퍼티의 이름은 "type"
)
@JsonSubTypes(
    // "type" 필드의 값에 따라 어떤 클래스를 생성할지 매핑
    JsonSubTypes.Type(value = InitPredictionTask::class, name = "init"),
    JsonSubTypes.Type(value = ArticlePredictionTask::class, name = "article"),
    JsonSubTypes.Type(value = ExponentPredictionTask::class, name = "exponent")
    // 다른 Task가 추가되면 여기에 계속 추가
)
sealed class PredictionTask {
    abstract val tickerId: UUID
    abstract val type: String
}


data class ArticlePredictionTask(
    override val tickerId: UUID,
    val payload: ArticlePayload
) : PredictionTask() {
    override val type: String = "article"

    data class ArticlePayload(
        val predictionDate: OffsetDateTime,
        val positiveArticleCount: Long,
        val negativeArticleCount: Long,
        val neutralArticleCount: Long,
        val createdAt: OffsetDateTime,
    )
}

data class InitPredictionTask(
    override val tickerId: UUID,
    val payload: InitPayload
) : PredictionTask() {
    override val type: String = "init"

    data class InitPayload(
        val tickerCode: String,
        val shortCompanyName: String,
        val predictionDate: OffsetDateTime,
        val todayMacd: Map<String, Double>,
        val yesterdayMacd: Map<String, Double>,
        val todayMa: Map<String, Double>,
        val yesterdayMa: Map<String, Double>,
        val todayRsi: Double,
        val todayHigh: Double,
        val todayLow: Double,
        val yesterdayClose: Double,
        val createdAt: OffsetDateTime,
    ) {
        lateinit var recentScores: List<Int>
        var yesterdayAtr: BigDecimal by Delegates.notNull()
    }
}

data class ExponentPredictionTask(
    override val tickerId: UUID,
    val payload: ExponentListPayload
) : PredictionTask() {
    override val type: String = "exponent"

    data class ExponentListPayload(
        val exponents: List<ExponentPayload>,
        val priceDate: OffsetDateTime,
        val predictionDate: OffsetDateTime
    ) {
        data class ExponentPayload(
            val exponentId: UUID,
            val code: String,
            val value: Double,
        ) {
            var previousValue: Double by Delegates.notNull()
        }

        lateinit var previousScores: List<TickerScore>
    }
}