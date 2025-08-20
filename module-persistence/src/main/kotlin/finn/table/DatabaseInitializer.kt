package finn.table

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

// 주의사항: prod 환경에서는 절대 사용하지 않기!
@Component
class DatabaseInitializer : ApplicationRunner {
    // 애플리케이션이 준비되면 이 run 메서드가 자동으로 호출됩니다.
    override fun run(args: ApplicationArguments?) {
        // 정의한 모든 Table 객체를 나열합니다.
        val tables =
            arrayOf(
                TickerTable,
                ArticleTable,
                PredictionTable,
                TickerPriceTable,
                NIntervalChangeRateTable,
                MarketStatusTable
            )

        // transaction 블록 안에서 테이블 생성을 시도합니다.
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*tables)
        }
    }
}