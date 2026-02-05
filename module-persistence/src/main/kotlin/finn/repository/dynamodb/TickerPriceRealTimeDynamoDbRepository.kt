package finn.repository.dynamodb

import finn.entity.dynamodb.TickerPriceRealTimeEntity
import finn.exception.NotFoundDataException
import finn.queryDto.PredictionListGraphDataQueryDto
import finn.queryDto.PredictionQueryDto
import finn.queryDto.TickerRealTimeGraphDataQueryDto
import finn.queryDto.TickerRealTimeHistoryGraphQueryDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Repository
class TickerPriceRealTimeDynamoDbRepository(
    private val dynamoDbClient: DynamoDbClient,
    @Value("\${dynamodb.table.ticker-price-real-time}") private val tableName: String
) {
    private val log = KotlinLogging.logger { }
    private val NY_ZONE = ZoneId.of("America/New_York")

    fun findLatestRealTimeData(
        tickerId: UUID,
        gte: Int?,
        missing: List<Int>?
    ): TickerRealTimeHistoryGraphQueryDto {
        // DynamoDB Query 요청 생성 (가장 최신 데이터 1개만 조회)
        val item = getLatestTickerPriceDataFromDb(tickerId)

        // 조회된 아이템을 DTO로 파싱 및 필터링
        val priceDateStr = item["priceDate"]?.s() ?: ""
        val maxLen = item["maxLen"]?.n()?.toInt() ?: 0
        val priceDate = LocalDate.parse(item["priceDate"]?.s().toString())

        val fullPriceDataList = item["priceDataList"]?.l()?.map { priceDataMapAttr ->
            val priceDataMap = priceDataMapAttr.m()
            val utcHours = priceDataMap["hours"]?.s() ?: "00:00:00"

            TickerRealTimeGraphDataQueryDto(
                price = BigDecimal(priceDataMap["price"]?.n()),
                hours = convertUtcTimeToKst(utcHours, priceDate),
                index = priceDataMap["index"]?.n()?.toInt() ?: 0
            )
        } ?: emptyList()

        val filteredPriceDataList = when {
            gte != null -> fullPriceDataList.filter { it.index >= gte }
            missing != null -> fullPriceDataList.filter { it.index in missing }
            else -> fullPriceDataList
        }

        return TickerRealTimeHistoryGraphQueryDto(
            priceDate = priceDateStr,
            tickerId = tickerId,
            priceDataList = filteredPriceDataList,
            maxLen = maxLen
        )
    }

    fun setLatestRealTimeDataForPrediction(predictionQueryDto: PredictionQueryDto) {
        val tickerId = predictionQueryDto.tickerId
        val item = getLatestTickerPriceDataFromDb(tickerId)

        // 조회된 아이템을 DTO로 파싱 및 필터링
        val priceDate = LocalDate.parse(item["priceDate"]?.s().toString())

        val fullPriceDataList = item["priceDataList"]?.l()?.map { priceDataMapAttr ->
            val priceDataMap = priceDataMapAttr.m()
            val utcHours = priceDataMap["hours"]?.s() ?: "00:00:00"

            TickerRealTimeGraphDataQueryDto(
                price = BigDecimal(priceDataMap["price"]?.n()),
                hours = convertUtcTimeToKst(utcHours, priceDate),
                index = priceDataMap["index"]?.n()?.toInt() ?: 0
            )
        }
            ?.sortedBy { it.hours } // 날짜 기준 오름차순 정렬 명시
            ?: emptyList()

        // 가장 최근 8개 주가 데이터를 추출
        val latest8PriceDataList = fullPriceDataList.takeLast(8).map {
            it.price
        }.toList()

        predictionQueryDto.graphData = PredictionListGraphDataQueryDto(true, latest8PriceDataList)
    }

    fun appendMinuteData(
        tickerId: UUID,
        entity: TickerPriceRealTimeEntity,
        index: Int,
        maxLen: Int
    ) {
        try {
            // 1. SK (날짜) 및 시간 포맷팅
            val zonedTime = entity.startTime.atZone(NY_ZONE)
            val dateKey = zonedTime.format(DateTimeFormatter.ISO_LOCAL_DATE) // 2026-01-30
            val timeString = zonedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) // 14:30:00

            // 2. 추가할 데이터 맵 생성 (스키마 준수)
            // 구조 반영
            val newMap = mapOf(
                "hours" to AttributeValue.builder().s(timeString).build(),
                "index" to AttributeValue.builder().n(index.toString()).build(),
                "price" to AttributeValue.builder().n(entity.close.toString()).build()
            )
            val newItemList = listOf(AttributeValue.builder().m(newMap).build())

            // 3. TTL 설정 (예: 7일 후 삭제)
            val ttl = System.currentTimeMillis() / 1000L + (7 * 24 * 60 * 60)

            // 4. UpdateItem 요청 (Upsert: 없으면 생성, 있으면 리스트 뒤에 붙임)
            val request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        "tickerId" to AttributeValue.builder().s(tickerId.toString()).build(),
                        "priceDate" to AttributeValue.builder().s(dateKey).build()
                    )
                )
                // 실제 컬럼명 대신 별칭(#maxLen, #ttl) 사용
                .updateExpression(
                    """
                SET priceDataList = list_append(if_not_exists(priceDataList, :emptyList), :newItem), 
                    #maxLen = :maxLen, 
                    #ttl = :ttl
                """.trimIndent()
                )
                // 별칭이 실제 어떤 컬럼인지 매핑
                .expressionAttributeNames(
                    mapOf(
                        "#maxLen" to "maxLen",
                        "#ttl" to "ttl" // ttl도 예약어 충돌 가능성이 있으므로 함께 처리
                    )
                )
                .expressionAttributeValues(
                    mapOf(
                        ":emptyList" to AttributeValue.builder().l(emptyList()).build(),
                        ":newItem" to AttributeValue.builder().l(newItemList).build(),
                        ":maxLen" to AttributeValue.builder().n(maxLen.toString()).build(),
                        ":ttl" to AttributeValue.builder().n(ttl.toString()).build()
                    )
                )
                .build()

            dynamoDbClient.updateItem(request)

        } catch (e: Exception) {
            log.error { "Failed to append data for $tickerId" + e.message }
        }
    }

    private fun getLatestTickerPriceDataFromDb(tickerId: UUID): Map<String, AttributeValue> {
        val queryRequest = QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression("tickerId = :v1")
            .expressionAttributeValues(
                mapOf(
                    ":v1" to AttributeValue.builder().s(tickerId.toString()).build()
                )
            )
            .scanIndexForward(false) // 정렬 키(priceDate) 기준 내림차순
            .limit(1) // 1개만 가져오기
            .build()

        return dynamoDbClient.query(queryRequest).items().firstOrNull()
            ?: throw NotFoundDataException("${tickerId}에 해당하는 최신 실시간 주가 데이터가 존재하지 않습니다. id 값을 확인해주세요.")
    }

    // 1. UTC 시간 문자열을 KST로 변환하는 헬퍼 함수
    private fun convertUtcTimeToKst(utcTimeStr: String, date: LocalDate): String {
        // (EST)와 같은 부가 정보가 있다면 제거
        val cleanUtcTimeStr = utcTimeStr.substringBefore("(")
        val utcTime = LocalTime.parse(cleanUtcTimeStr)

        // 날짜와 시간을 합쳐 UTC 기준 LocalDateTime 생성
        val utcDateTime = LocalDateTime.of(date, utcTime)

        // UTC -> KST로 변환
        val kstDateTime = utcDateTime.atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime()

        // KST 시간 부분만 HH:mm:ss 형식으로 반환
        return kstDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }
}