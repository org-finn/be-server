package finn.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

// Table 객체 정의
object TickerTable : UUIDTable("ticker") {
    val code = varchar("code", 20).uniqueIndex()
    val fullCompanyName = varchar("full_company_name", 100)
    val country = varchar("country", 100)
    val shortCompanyName = varchar("short_company_name", 100)
    val category = varchar("category", 50).nullable()
    val marketCap = long("market_cap").nullable()
    val createdAt = datetime("created_at")
}

    object NewsTable : UUIDTable("news") {
        val publishedDate = datetime("published_date")
        val title = varchar("title", 255)
        val description = varchar("description", 255)
        val newsUrl = text("news_url").uniqueIndex()
        val thumbnailUrl = text("thumbnail_url").nullable()
        val viewCount = long("view_count").default(0L)
        val likeCount = long("like_count").default(0L)
        val sentiment = varchar("sentiment", 20)
        val reasoning = varchar("reasoning", 255).nullable()
        val shortCompanyName = varchar("short_company_name", 100)
        val author = varchar("author", 50)
        val distinctId = varchar("distinct_id", 100).uniqueIndex()
        val tickerId = uuid("ticker_id")
        val tickerCode = varchar("ticker_code", 20)
        val createdAt = datetime("created_at")
    }

object PredictionTable : UUIDTable("predictions") {
    val predictionDate = datetime("prediction_date")
    val positiveNewsCount = long("positive_news_count")
    val negativeNewsCount = long("negative_news_count")
    val neutralNewsCount = long("neutral_news_count")
    val sentiment = integer("sentiment")
    val strategy = varchar("strategy", 50)
    val score = integer("score")
    val tickerCode = varchar("ticker_code", 20)
    val shortCompanyName = varchar("short_company_name", 100)
    val tickerId = uuid("ticker_id")
    val createdAt = datetime("created_at")
}

object TickerPriceTable : UUIDTable("ticker_prices") {
    val priceDate = date("price_date")
    val open = decimal("open", 10, 4)
    val high = decimal("high", 10, 4)
    val low = decimal("low", 10, 4)
    val close = decimal("close", 10, 4)
    val volume = long("volume")
    val tickerCode = varchar("ticker_code", 20)
    val tickerId = uuid("ticker_id")
    val createdAt = datetime("created_at")

    init {
        uniqueIndex("ticker_prices_ticker_id_price_date_unique_key", tickerId, priceDate)
    }
}

object NIntervalChangeRateTable : UUIDTable("n_interval_change_rates") {
    val interval = integer("interval")
    val priceDate = date("price_date")
    val changeRate = decimal("change_rate", 5, 2)
    val tickerCode = varchar("ticker_code", 20)
    val tickerId = uuid("ticker_id")
    val createdAt = datetime("created_at")
}

// MarketStatus 테이블
object MarketStatusTable : LongIdTable("market_status") {
    val date = date("date").uniqueIndex()
    val tradingHours = varchar("trading_hours", 20)
    val eventName = varchar("event_name", 50).nullable()
}