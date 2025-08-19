package finn.modulePersistence.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

// Ticker 테이블
object TickerTable : Table("ticker") {
    val id = uuid("id").autoGenerate()
    val code = varchar("code", 20).uniqueIndex()
    val fullCompanyName = varchar("full_company_name", 100)
    val country = varchar("country", 100)
    val shortCompanyName = varchar("short_company_name", 100).nullable()
    val category = varchar("category", 50).nullable()
    val createdAt = date("created_at")

    override val primaryKey = PrimaryKey(id)
}

// News 테이블
object NewsTable : Table("news") {
    val id = uuid("id").autoGenerate()
    val publishedDate = date("published_date")
    val title = varchar("title", 255)
    val description = varchar("description", 255)
    val newsUrl = text("news_url").uniqueIndex()
    val imageUrl = text("image_url").nullable()
    val viewCount = long("view_count").default(0L)
    val likeCount = long("like_count").default(0L)
    val sentiment = varchar("sentiment", 20)
    val sentimentReasoning = varchar("sentiment_reasoning", 255).nullable()
    val shortCompanyName = varchar("short_company_name", 100)
    val author = varchar("author", 50)
    val distinctId = varchar("distinct_id", 100).uniqueIndex()
    val tickerId = uuid("ticker_id")
    val tickerCode = varchar("ticker_code", 20)
    val createdAt = date("created_at")

    override val primaryKey = PrimaryKey(id)
}

// Predictions 테이블
object PredictionTable : Table("predictions") {
    val id = uuid("id").autoGenerate()
    val predictionDate = date("prediction_date")
    val positiveNewsCount = long("positive_news_count")
    val negativeNewsCount = long("negative_news_count")
    val neutralNewsCount = long("neutral_news_count")
    val score = decimal("score", 5, 2)
    val tickerCode = varchar("ticker_code", 20)
    val shortCompanyName = varchar("short_company_name", 100)
    val tickerId = uuid("ticker_id")
    val createdAt = date("created_at")

    override val primaryKey = PrimaryKey(id)
}

// TickerPrices 테이블
object TickerPriceTable : Table("ticker_prices") {
    val id = uuid("id").autoGenerate()
    val priceDate = date("price_date")
    val open = decimal("open", 10, 4).nullable()
    val high = decimal("high", 10, 4).nullable()
    val low = decimal("low", 10, 4).nullable()
    val close = decimal("close", 10, 4).nullable()
    val volume = long("volume").nullable()
    val tickerCode = varchar("ticker_code", 20)
    val tickerId = uuid("ticker_id")
    val createdAt = date("created_at")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("ticker_prices_ticker_id_price_date_unique_key",
            tickerId, priceDate)
    }
}

// NIntervalChangeRates 테이블
object NIntervalChangeRateTable : Table("n_interval_change_rates") {
    val id = uuid("id").autoGenerate()
    val interval = integer("interval")
    val priceDate = date("price_date")
    val changeRate = decimal("change_rate", 5, 2)
    val tickerCode = varchar("ticker_code", 20)
    val tickerId = uuid("ticker_id")
    val createdAt = date("created_at")

    override val primaryKey = PrimaryKey(id)
}

// MarketStatus 테이블
object MarketStatusTable : Table("market_status") {
    val id = long("id").autoIncrement()
    val date = date("date").uniqueIndex()
    val tradingHours = varchar("trading_hours", 20).nullable()
    val eventName = varchar("event_name", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}