package finn.repository.dynamodb

import finn.exception.NotFoundDataException
import finn.queryDto.PredictionListGraphDataQueryDto
import finn.queryDto.PredictionQueryDto
import finn.queryDto.TickerRealTimeGraphDataQueryDto
import finn.queryDto.TickerRealTimeGraphQueryDto
import org.springframework.stereotype.Repository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Repository
class TickerPriceRealTimeDynamoDbRepository(
    private val dynamoDbClient: DynamoDbClient
) {
    private val tableName = "ticker_price_real_time"

    fun findLatestRealTimeData(
        tickerId: UUID,
        gte: Int?,
        missing: List<Int>?
    ): TickerRealTimeGraphQueryDto {
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

        return TickerRealTimeGraphQueryDto(
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