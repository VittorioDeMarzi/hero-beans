package techcourse.herobeans

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class HeroBeansApplication

fun main(args: Array<String>) {
    runApplication<HeroBeansApplication>(*args)
    println("hello world")
}
