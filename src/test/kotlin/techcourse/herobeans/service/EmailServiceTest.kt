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
import techcourse.herobeans.entity.Order
import techcourse.herobeans.entity.OrderItem
import techcourse.herobeans.enums.ShippingMethod
import java.math.BigDecimal
import java.time.LocalDateTime

@ActiveProfiles("test")
class EmailServiceTest {
    private val mailSender = mockk<JavaMailSender>()
    private val templateEngine = mockk<TemplateEngine>()
    private val mimeMessage = mockk<MimeMessage>(relaxed = true)

    private lateinit var emailService: EmailService

    @BeforeEach
    fun setUp() {
        emailService = EmailService(mailSender, templateEngine, "testHerobeans@email.com")
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

    @Test
    fun `after order should send confirmation email successfully`() {
        val userEmail = "customer@example.com"
        val order =
            Order(
                id = 12345L,
                createdAt = LocalDateTime.now(),
                orderItems =
                    listOf(
                        OrderItem(10, "Specialty Coffee Beans", "250g", 2, BigDecimal("15.50")),
                    ) as MutableList<OrderItem>,
                coffeeSubTotal = BigDecimal("31.00"),
                shippingMethod = ShippingMethod.STANDARD,
                shippingFee = BigDecimal("5.00"),
                memberId = 10,
            )
        val expectedHtmlContent = "<html>Your order #12345 is confirmed</html>"

        every { templateEngine.process("order-confirmation-email", any<Context>()) } returns expectedHtmlContent
        every { mailSender.createMimeMessage() } returns mimeMessage
        every { mailSender.send(mimeMessage) } just Runs

        // When
        emailService.sendOrderConfirmationEmail(userEmail, order)

        // Then
        verify {
            templateEngine.process(
                eq("order-confirmation-email"),
                match<Context> { context ->
                    context.getVariable("order") == order
                },
            )
        }
        verify { mailSender.createMimeMessage() }
        verify { mailSender.send(mimeMessage) }
    }

    @Test
    fun `sendOrderConfirmationEmail should throw runtime if template processing fails`() {
        // Given
        val userEmail = "customer@example.com"
        val order =
            Order(
                id = 54321L,
                createdAt = LocalDateTime.now(),
                orderItems =
                    listOf(
                        OrderItem(10, "Specialty Coffee Beans", "250g", 2, BigDecimal("15.50")),
                    ) as MutableList<OrderItem>,
                coffeeSubTotal = BigDecimal("31.00"),
                shippingMethod = ShippingMethod.STANDARD,
                shippingFee = BigDecimal("5.00"),
                memberId = 10,
            )

        every { templateEngine.process(any<String>(), any<Context>()) } throws RuntimeException("Template error")

        // When & Then
        val exception =
            assertThrows<RuntimeException> {
                emailService.sendOrderConfirmationEmail(userEmail, order)
            }

        assertEquals("Email confirmation failed", exception.message)

        // Verify that no email was sent
        verify(exactly = 0) { mailSender.send(any<MimeMessage>()) }
    }
}
