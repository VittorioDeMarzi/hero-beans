package techcourse.herobeans.configuration

import io.github.cdimascio.dotenv.Dotenv
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

@Configuration
class DotenvConfig {
    @PostConstruct
    fun loadDotenv() {
        try {
            val dotenv =
                Dotenv.configure()
                    .ignoreIfMissing()
                    .ignoreIfMalformed()
                    .load()

            dotenv.entries().forEach { System.setProperty(it.key, it.value) }
            log.info("Loaded ${dotenv.entries().size} environment variables from .env file")
        } catch (e: Exception) {
            log.warn("Failed to load .env file: ${e.message}")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DotenvConfig::class.java)
    }
}
