package finn.moduleApp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["finn.moduleApp",
        "finn.modulePersistence", "finn.moduleCommon",
        "finn.moduleApi", "finn.moduleDomain"]
)
class ModuleAppApplication

fun main(args: Array<String>) {
    runApplication<ModuleAppApplication>(*args)
}
