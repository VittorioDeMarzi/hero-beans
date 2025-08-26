package techcourse.herobeans.service

import jakarta.mail.internet.MimeMessage
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import techcourse.herobeans.entity.Order

private val log = KotlinLogging.logger { }

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    @Value("\${spring.mail.username}") private val fromEmail: String,
) {
    fun sendRegistrationEmail(
        userEmail: String,
        userName: String,
        code: String,
    ) {
        try {
            val context = Context()
            context.setVariable("userName", userName)
            context.setVariable("couponCode", code)

            val htmlContent = templateEngine.process("registration-email", context)

            val message = createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom("team@herobeans.com", "HeroBeans Ecommerce")
            helper.setTo(userEmail)
            helper.setSubject("Welcome to our coffee store!")
            helper.setText(htmlContent, true)

            mailSender.send(message)
            log.info("Registration email sent successfully to: $userEmail")
        } catch (e: Exception) {
            log.error("Failed to send registration email to $userEmail", e)
            throw RuntimeException("Email service error", e)
        }
    }

    fun sendOrderConfirmationEmail(
        userEmail: String,
        order: Order,
    ) {
        try {
            val context = Context()
            context.setVariable("order", order)

            val htmlContent = templateEngine.process("order-confirmation-email", context)

            val message = createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(fromEmail, "HeroBeans Ecommerce")
            helper.setTo(userEmail)
            helper.setSubject("Order Confirmation #${order.id}")
            helper.setText(htmlContent, true)

            mailSender.send(message)
            log.info("Confirmation email sent for order #${order.id} to: $userEmail")
        } catch (e: Exception) {
            log.error("Error sending confirmation email for order #${order.id} to $userEmail", e)
            throw RuntimeException("Email confirmation failed", e)
        }
    }

    private fun createMimeMessage(): MimeMessage = mailSender.createMimeMessage()
}
