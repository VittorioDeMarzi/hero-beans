package techcourse.herobeans

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import techcourse.herobeans.config.StripeProperties

@EnableConfigurationProperties(StripeProperties::class)
@SpringBootApplication
@EnableJpaAuditing
class HeroBeansApplication

fun main(args: Array<String>) {
    runApplication<HeroBeansApplication>(*args)
}
