package techcourse.herobeans.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@ActiveProfiles("test")
class EmailServiceTest {
    private val mailSender = mockk<JavaMailSender>()
    private val templateEngine = mockk<TemplateEngine>()
    private val mimeMessage = mockk<MimeMessage>(relaxed = true)

    private lateinit var emailService: EmailService

    @BeforeEach
    fun setUp() {
        emailService = EmailService(mailSender, templateEngine)
    }

    @Test
    fun `sendRegistrationEmail should send email successfully`() {
        // Given
        val userEmail = "test@tes.com"
        val userName = "Test User"
        val code = "WELCOME123"
        val expectedHtmlContent = "<html>Welcome, $userName! Your code is $code</html>"

        every { templateEngine.process("registration-email", any<Context>()) } returns expectedHtmlContent
        every { mailSender.createMimeMessage() } returns mimeMessage
        every { mailSender.send(mimeMessage) } just Runs

        emailService.sendRegistrationEmail(userEmail, userName, code)

        verify {
            templateEngine.process(
                "registration-email",
                match<Context> { context ->
                    context.getVariable("userName") == userName &&
                        context.getVariable("couponCode") == code
                },
            )
        }
        verify { mailSender.createMimeMessage() }
        verify { mailSender.send(mimeMessage) }
    }

    @Test
    fun `should throw runtime if email sending fail`() {
        // Given
        val userEmail = "user@example.com"
        val userName = "Mario Rossi"
        val couponCode = "WELCOME10"

        every { templateEngine.process(any<String>(), any<Context>()) } throws RuntimeException("Template error")

        val exception =
            assertThrows<RuntimeException> {
                emailService.sendRegistrationEmail(userEmail, userName, couponCode)
            }

        assertEquals("Email service error", exception.message)

        verify(exactly = 0) { mailSender.send(any<MimeMessage>()) }
    }
}
