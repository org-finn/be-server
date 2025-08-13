package finn.articker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ArtickerApplication

fun main(args: Array<String>) {
	runApplication<ArtickerApplication>(*args)
}
