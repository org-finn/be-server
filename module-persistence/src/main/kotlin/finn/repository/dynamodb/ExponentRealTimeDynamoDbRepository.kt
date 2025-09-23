package finn.repository.dynamodb

import finn.exception.NotFoundDataException
import finn.task.ExponentPredictionTask.ExponentListPayload.ExponentPayload
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Repository
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Repository
class ExponentRealTimeDynamoDbRepository(
    private val dynamoDbAsyncClient: DynamoDbAsyncClient
) {
    private val tableName = "exponent_real_time"

    suspend fun findAndSetPreviousValues(
        exponents: List<ExponentPayload>,
        priceDate: LocalDateTime
    ) {
        // 코루틴 스코프를 사용하여 여러 비동기 작업을 관리합니다.
        coroutineScope {
            // 1. 각 exponent에 대해 비동기적으로 과거 데이터 조회 작업을 시작합니다.
            val deferredPreviousValues = exponents.map { exponent ->
                async {
                    findPreviousValueForExponent(exponent.exponentId, priceDate)
                }
            }

            // 2. 모든 비동기 작업이 완료될 때까지 기다리고 결과를 가져옵니다.
            val previousValues = deferredPreviousValues.awaitAll()

            // 3. 가져온 결과를 각 ExponentPayload의 previousValue에 할당합니다.
            exponents.forEachIndexed { index, exponentPayload ->
                exponentPayload.previousValue = previousValues[index]
            }
        }
    }

    /**
     * 단일 exponentId에 대해 30분 전 데이터를 조회하는 private 헬퍼 함수입니다.
     */
    private suspend fun findPreviousValueForExponent(
        exponentId: UUID,
        priceDate: LocalDateTime
    ): Double {
        val queryRequest = QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression("exponentId = :v1")
            .expressionAttributeValues(
                mapOf(":v1" to AttributeValue.builder().s(exponentId.toString()).build())
            )
            .scanIndexForward(false)
            .limit(1)
            .build()

        val item = dynamoDbAsyncClient.query(queryRequest).await().items().firstOrNull()
            ?: throw NotFoundDataException("${exponentId}에 해당하는 최신 데이터가 없습니다.")

        val targetTime = priceDate.minusMinutes(30).toLocalTime()
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