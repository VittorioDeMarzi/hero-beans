package techcourse.herobeans.service

import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
) {
    @Value("\${spring.mail.username}")
    private lateinit var fromEmail: String

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendRegistrationEmail(
        userEmail: String,
        userName: String,
    ) {
        try {
            val message = createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(fromEmail, "HeroBeans Ecommerce")
            helper.setTo(userEmail)
            helper.setSubject("Benvenuto nel nostro store!")

            val htmlContent =
                """
                <html>
                <body>
                    <h2>Hi ${'$'}userName!</h2>
                    <p>Welcome to our ecommerce store!</p>
                    <p>Your registration has been completed successfully.</p>
                    <p>You can start shopping on our site now.</p>
                    <br>
                    <p>Thank you for choosing our store!</p>
                    <p><strong>The Ecommerce Team</strong></p>
                </body>
                </html>
                """.trimIndent()

            helper.setText(htmlContent, true)
            mailSender.send(message)
            logger.info("Email not sent: $userEmail")
        } catch (e: Exception) {
            logger.error("Email not sent $userEmail", e)
            throw RuntimeException("Email service error", e)
        }
    }

    fun sendOrderConfirmationEmail(
        userEmail: String,
        userName: String,
        orderId: String,
        orderTotal: Double,
        items: List<String>,
    ) {
        try {
            val message = createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(fromEmail, "HeroBeans Ecommerce")
            helper.setTo(userEmail)
            helper.setSubject("Conferma Ordine #$orderId")

            val itemsList = items.joinToString("") { "<li>$it</li>" }

            val htmlContent =
                """
                <html>
                <body>
                    <h2>Ciao $userName!</h2>
                    <p>Grazie per il tuo ordine!</p>
                    
                    <div style="border: 1px solid #ccc; padding: 15px; margin: 20px 0;">
                        <h3>Dettagli Ordine</h3>
                        <p><strong>Numero Ordine:</strong> #$orderId</p>
                        <p><strong>Totale:</strong> €${String.format("%.2f", orderTotal)}</p>
                        
                        <h4>Prodotti ordinati:</h4>
                        <ul>$itemsList</ul>
                    </div>
                    
                    <p>Riceverai un'email di tracking quando l'ordine verrà spedito.</p>
                    
                    <p>Grazie per aver scelto il nostro store!</p>
                    <p><strong>Il Team Ecommerce</strong></p>
                </body>
                </html>
                """.trimIndent()

            helper.setText(htmlContent, true)
            mailSender.send(message)

            logger.info("Email conferma ordine inviata a: $userEmail per ordine: $orderId")
        } catch (e: Exception) {
            logger.error("Errore invio email ordine $orderId a $userEmail", e)
            throw RuntimeException("Errore invio email", e)
        }
    }

    private fun createMimeMessage(): MimeMessage = mailSender.createMimeMessage()
}
