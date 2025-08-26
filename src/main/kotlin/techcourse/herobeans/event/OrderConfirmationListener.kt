package techcourse.herobeans.event

import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import techcourse.herobeans.service.EmailService

private val log = KotlinLogging.logger {}

class OrderConfirmationListener {
    @Component
    class OrderConfirmationListener(private val emailService: EmailService) {
        @EventListener
        fun handleOrderConfirmationEvent(event: OrderConfirmationEvent) {
            try {
                emailService
                    .sendOrderConfirmationEmail(event.member.email, event.order)
                    .also { log.info { "Order ${event.order.id} - Successfully sent order confirmation email to: $event.member.email" } }
            } catch (e: Exception) {
                log.error(e) { "Order ${event.order.id} - Error while sending order confirmation email to: $event.member.email" }
            }
        }
    }
}
