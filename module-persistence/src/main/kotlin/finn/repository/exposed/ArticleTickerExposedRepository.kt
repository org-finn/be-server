package finn.repository.exposed

import finn.insertDto.ArticleTickerToInsert
import finn.table.ArticleTickerTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.springframework.stereotype.Repository

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
            this[ArticleTickerTable.title] = it.title
            this[ArticleTickerTable.sentiment] = it.sentiment
            this[ArticleTickerTable.reasoning] = it.reasoning
        }.size
        log.debug { "${insertedCount} / ${toInserts.size} 개의 ArticleTicker 데이터를 성공적으로 저장하였습니다." }
    }
}