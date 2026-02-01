package finn

import finn.config.GoogleOAuthConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(GoogleOAuthConfig::class)
class ModuleAppApplication

fun main(args: Array<String>) {
    runApplication<ModuleAppApplication>(*args)
}
