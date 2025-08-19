package finn

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ModuleAppApplication

fun main(args: Array<String>) {
    runApplication<ModuleAppApplication>(*args)
}
