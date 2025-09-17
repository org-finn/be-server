package finn.score.task

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,       // 타입을 식별하기 위해 이름을 사용
    include = JsonTypeInfo.As.PROPERTY, // JSON 프로퍼티(필드)를 보고 타입을 결정
    property = "type"                 // 그 프로퍼티의 이름은 "type"
)
@JsonSubTypes(
    // "type" 필드의 값에 따라 어떤 클래스를 생성할지 매핑
    JsonSubTypes.Type(value = InitPredictionTask::class, name = "init"),
    JsonSubTypes.Type(value = ArticlePredictionTask::class, name = "article")
    // 다른 Task가 추가되면 여기에 계속 추가
)
sealed class PredictionTask {
    abstract val tickerId: UUID
    abstract val type: String
}