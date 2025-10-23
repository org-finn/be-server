package finn.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.timestamp

// Table 객체 정의
object TickerTable : UUIDTable("ticker") {
    val code = varchar("code", 20).uniqueIndex()
    val fullCompanyName = varchar("full_company_name", 100)
    val country = varchar("country", 100)
    val shortCompanyName = varchar("short_company_name", 100)
    val shortCompanyNameKr = varchar("short_company_name_kr", 100)
    val category = varchar("category", 50).nullable()
    val marketCap = long("market_cap").nullable()
    val exchangeCode = varchar("exchange_code", 20)
    val createdAt = datetime("created_at")
}

object ExponentTable : UUIDTable("exponent") {
    val code = varchar("code", 20).uniqueIndex()
    val name = varchar("name", 100)
    val country = varchar("country", 100)
}

object ArticleTable : UUIDTable("article") {
    val publishedDate = timestamp("published_date")
    val title = text("title")
    val description = text("description")
    val articleUrl = text("article_url").uniqueIndex()
    val thumbnailUrl = text("thumbnail_url").nullable()
    val viewCount = long("view_count").default(0L)
    val likeCount = long("like_count").default(0L)
    val author = varchar("author", 100)
    val distinctId = varchar("distinct_id", 255).uniqueIndex()
    val tickers = varchar("tickers", 255).nullable()
    val createdAt = datetime("created_at")
}

object ArticleTickerTable : UUIDTable("article_ticker") {
    val articleId = uuid("article_id")
    val tickerId = uuid("ticker_id")
    val tickerCode = varchar("ticker_code", 20)
    val shortCompanyName = varchar("short_company_name", 100)
    val title = text("title")
    val sentiment = varchar("sentiment", 20).nullable()
    val reasoning = text("reasoning").nullable()
    val publishedDate = timestamp("published_date")
    val createdAt = datetime("created_at")

    init {
        Index(listOf(tickerId), false, "article_ticker_ticker_id_idx")
    }
}

object PredictionTable : UUIDTable("predictions") {
    val predictionDate = datetime("prediction_date") // EST
    val positiveArticleCount = long("positive_article_count")
    val negativeArticleCount = long("negative_article_count")
    val neutralArticleCount = long("neutral_article_count")
    val sentiment = integer("sentiment")
    val strategy = varchar("strategy", 50)
    val score = integer("score")
    val volatility = double("volatility")
    val tickerCode = varchar("ticker_code", 20)
    val shortCompanyName = varchar("short_company_name", 100)
    val tickerId = uuid("ticker_id")
    val createdAt = datetime("created_at")
    init {
        uniqueIndex("predictions_ticker_id_prediction_date_unique_key",
            tickerId, predictionDate)
    }
}

object TickerPriceTable : UUIDTable("ticker_prices") {
    val priceDate = datetime("price_date")
    val open = decimal("open", 10, 4)
    val high = decimal("high", 10, 4)
    val low = decimal("low", 10, 4)
    val close = decimal("close", 10, 4)
    val volume = long("volume")
    val atr = double("atr")
    val changeRate = decimal("change_rate", 7, 4)
    val tickerCode = varchar("ticker_code", 20)
    val tickerId = uuid("ticker_id")
    val createdAt = datetime("created_at")

    init {
        uniqueIndex("ticker_prices_ticker_id_price_date_unique_key", tickerId, priceDate)
    }
}

// MarketStatus 테이블
object MarketStatusTable : LongIdTable("market_status") {
    val date = date("date").uniqueIndex()
    val tradingHours = varchar("trading_hours", 20)
    val eventName = varchar("event_name", 50).nullable()
}