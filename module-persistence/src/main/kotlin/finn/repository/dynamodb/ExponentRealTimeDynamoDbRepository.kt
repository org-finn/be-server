package finn.repository.dynamodb

import finn.exception.NotFoundDataException
import org.springframework.stereotype.Repository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Repository
class ExponentRealTimeDynamoDbRepository(
    private val dynamoDbClient: DynamoDbClient
) {
    private val tableName = "exponent_real_time"

    suspend fun findRealTimeRecentPriceData(
        exponentId: UUID,
        priceDate: LocalDateTime
    ): Double {
        // DynamoDB Query 요청 생성 (가장 최신 데이터 1개만 조회)
        val queryRequest = QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression("exponentId = :v1")
            .expressionAttributeValues(
                mapOf(
                    ":v1" to AttributeValue.builder().s(exponentId.toString()).build()
                )
            )
            .scanIndexForward(false) // 정렬 키(priceDate) 기준 내림차순
            .limit(1)                // 1개만 가져오기
            .build()

        val item = dynamoDbClient.query(queryRequest).items().firstOrNull()
            ?: throw NotFoundDataException("${exponentId}에 해당하는 최신 데이터가 없습니다.")

        // 2. 30분 전 시간 계산 (시간만)
        val targetTime = priceDate.minusMinutes(30).toLocalTime()

        // 3. priceDataList에서 targetTime에 가장 가까운 항목 찾기
        val priceDataList = item["priceDataList"]?.l() ?: emptyList()

        val closestPriceData = priceDataList.minByOrNull { priceDataMapAttr ->
            val hoursString = priceDataMapAttr.m()["hours"]?.s() ?: ""
            val dataTime = LocalTime.parse(hoursString, DateTimeFormatter.ISO_LOCAL_TIME)
            // 시간 차이의 절댓값을 계산하여 가장 작은 것을 찾음
            kotlin.math.abs(java.time.Duration.between(dataTime, targetTime).seconds)
        } ?: throw NotFoundDataException("30분 전 데이터에 근접한 항목이 없습니다.")

        return closestPriceData.m()["price"]!!.n().toDouble()
    }
}