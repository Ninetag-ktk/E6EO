package e6eo.finalproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
@EnableMongoRepositories(basePackages = ["e6eo.finalproject.dto"])
class FinalProjectApplication

fun main(args: Array<String>) {
    runApplication<FinalProjectApplication>(*args)
}