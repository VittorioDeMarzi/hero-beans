package techcourse.herobeans.sliceTest

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import techcourse.herobeans.exception.GlobalExceptionHandler

@ActiveProfiles("test")
class GlobalExceptionHandlerLoggingTest {
    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(FakeExceptionController())
            .setControllerAdvice(GlobalExceptionHandler())
            .build()

    @Test
    fun `logs ERROR with path and message`() {
        val logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler::class.java) as Logger
        val appender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(appender)

        mockMvc.perform(get("/dummy/generic"))
            .andExpect(status().isInternalServerError)

        val messages = appender.list.filter { it.level == Level.ERROR }.map { it.formattedMessage }
        assert(messages.any { it.contains("Exception at GET /dummy/generic") })
        assert(messages.any { it.contains("Boom") })

        logger.detachAppender(appender)
    }
}
