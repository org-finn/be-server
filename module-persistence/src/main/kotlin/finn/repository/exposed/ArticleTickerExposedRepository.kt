package finn.repository.exposed

import finn.insertDto.ArticleTickerToInsert
import finn.table.ArticleTickerTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId

@Repository
class ArticleTickerExposedRepository {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun saveAll(toInserts: List<ArticleTickerToInsert>) {
        val insertedCount = ArticleTickerTable.batchInsert(
            toInserts,
            ignore = true // unique 위반 조건 데이터는 삽입 건너뜀
        ) {
            this[ArticleTickerTable.articleId] = it.articleId
            this[ArticleTickerTable.tickerId] = it.tickerId
            this[ArticleTickerTable.tickerCode] = it.tickerCode
            this[ArticleTickerTable.title] = it.title
            this[ArticleTickerTable.sentiment] = it.sentiment
            this[ArticleTickerTable.reasoning] = it.reasoning
            this[ArticleTickerTable.publishedDate] = it.publishedDate
            this[ArticleTickerTable.createdAt] = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        }.size
        log.debug { "${insertedCount} / ${toInserts.size} 개의 ArticleTicker 데이터를 성공적으로 저장하였습니다." }
    }
}