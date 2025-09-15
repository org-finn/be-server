package finn.table

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev", "test")// dev, 테스트 환경에서만 활성화
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
                MarketStatusTable,
                ArticleTickerTable
            )

        // transaction 블록 안에서 테이블 생성을 시도합니다.
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*tables)
        }
    }
}