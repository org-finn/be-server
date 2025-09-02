package finn.repository.dynamodb

import finn.exception.CriticalDataOmittedException
import finn.queryDto.TickerRealTimeGraphDataQueryDto
import finn.queryDto.TickerRealTimeGraphQueryDto
import org.springframework.stereotype.Repository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.math.BigDecimal
import java.util.*

@Repository
class TickerPriceRealTimeDynamoDbRepository(
    private val dynamoDbClient: DynamoDbClient
) {
    private val tableName = "ticker_price_real_time"

    private data class TickerRealTimeGraphDataQueryDtoImpl(
        val price: BigDecimal,
        val hours: String,
        val index: Int
    ) : TickerRealTimeGraphDataQueryDto {
        override fun price(): BigDecimal = this.price
        override fun hours(): String = this.hours
        override fun index(): Int = this.index
    }

    private data class TickerRealTimeGraphQueryDtoImpl(
        val priceDate: String,
        val tickerId: UUID,
        val priceDataList: List<TickerRealTimeGraphDataQueryDto>,
        val maxLen: Int
    ) : TickerRealTimeGraphQueryDto {
        override fun priceDate(): String = this.priceDate
        override fun tickerId(): UUID = this.tickerId
        override fun priceDataList(): List<TickerRealTimeGraphDataQueryDto> = this.priceDataList
        override fun maxLen(): Int = this.maxLen
    }


    fun getLatestRealTimeData(
        tickerId: UUID,
        gte: Int?,
        missing: List<Int>?
    ): TickerRealTimeGraphQueryDto {
        // 2. DynamoDB Query 요청 생성 (가장 최신 데이터 1개만 조회)
        val queryRequest = QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression("tickerId = :v1")
            .expressionAttributeValues(
                mapOf(
                    ":v1" to AttributeValue.builder().s(tickerId.toString()).build()
                )
            )
            .scanIndexForward(false) // 정렬 키(priceDate) 기준 내림차순
            .limit(1)                // 1개만 가져오기
            .build()

        val item = dynamoDbClient.query(queryRequest).items().firstOrNull()
            ?: throw CriticalDataOmittedException("${tickerId}에 해당하는 최신 실시간 주가 데이터가 존재하지 않습니다.")

        // 3. 조회된 아이템을 새로운 private DTO로 파싱 및 필터링
        val priceDateStr = item["priceDate"]?.s() ?: ""
        val maxLen = item["maxLen"]?.n()?.toInt() ?: 0
        val fullPriceDataList = item["priceDataList"]?.l()?.map { priceDataMapAttr ->
            val priceDataMap = priceDataMapAttr.m()
            TickerRealTimeGraphDataQueryDtoImpl(
                price = BigDecimal(priceDataMap["price"]?.s()),
                hours = priceDataMap["hours"]?.s() ?: "",
                index = priceDataMap["index"]?.n()?.toInt() ?: 0
            )
        } ?: emptyList()

        val filteredPriceDataList = when {
            gte != null -> fullPriceDataList.filter { it.index >= gte }
            missing != null -> fullPriceDataList.filter { it.index in missing }
            else -> fullPriceDataList
        }

        // 4. 최종적으로 인터페이스를 구현한 DTOImpl 객체를 반환
        return TickerRealTimeGraphQueryDtoImpl(
            priceDate = priceDateStr,
            tickerId = tickerId,
            priceDataList = filteredPriceDataList,
            maxLen = maxLen
        )
    }
}