package techcourse.herobeans.service

import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
) {
    @Value("\${spring.mail.username}")
    private lateinit var fromEmail: String

    // TODO Logging
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

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
            logger.info("Registration email sent successfully to: $userEmail")
        } catch (e: Exception) {
            logger.error("Failed to send registration email to $userEmail", e)
            throw RuntimeException("Email service error", e)
        }
    }

    private fun createMimeMessage(): MimeMessage = mailSender.createMimeMessage()
}
