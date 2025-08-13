package finn.articker

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<ArtickerApplication>().with(TestcontainersConfiguration::class).run(*args)
}
