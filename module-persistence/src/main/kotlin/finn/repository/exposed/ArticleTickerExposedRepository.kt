package finn.repository.exposed

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Repository

@Repository
class ArticleTickerExposedRepository {
    companion object {
        private val log = KotlinLogging.logger {}
    }

}