package techcourse.herobeans.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.client.StripeClient
import techcourse.herobeans.dto.CheckoutStartRequest
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.entity.Order
import techcourse.herobeans.entity.Payment
import techcourse.herobeans.enums.PaymentStatus
import techcourse.herobeans.exception.PaymentIntentNotFoundException
import techcourse.herobeans.exception.PaymentNotFoundException
import techcourse.herobeans.repository.PaymentJpaRepository
import java.math.BigDecimal
import java.math.RoundingMode

private val log = KotlinLogging.logger {}

@Service
class PaymentService(
    private val stripeClient: StripeClient,
    private val paymentRepository: PaymentJpaRepository,
) {
    @Transactional
    fun createPaymentIntent(
        request: CheckoutStartRequest,
        totalAmount: BigDecimal,
    ): PaymentIntent {
        log.info { "payment.intent.create.started paymentMethod=${request.paymentMethod} amount=$totalAmount" }
        val paymentIntent = stripeClient.createPaymentIntent(request, totalAmount)
        log.info { "payment.intent.create.success paymentIntentId=${paymentIntent.id} amount=${paymentIntent.amount}" }
        return paymentIntent
    }

    fun confirmPaymentIntent(paymentIntentId: String): PaymentIntent {
        log.info { "payment.intent.confirm.started paymentIntentId=$paymentIntentId" }
        require(paymentRepository.existsByPaymentIntentId(paymentIntentId)) {
            throw PaymentIntentNotFoundException("payment intent with id $paymentIntentId not found")
        }
        val paymentIntent = stripeClient.confirmPaymentIntent(paymentIntentId)
        log.info { "payment.intent.confirm.success paymentIntentId=$paymentIntentId status=${paymentIntent.status}" }
        return paymentIntent
    }

    fun isPaymentSucceeded(paymentIntent: PaymentIntent): Boolean {
        log.info { "payment.status.check.started paymentIntentId=${paymentIntent.id}" }
        val succeeded = paymentIntent.status == "succeeded"
        log.info { "payment.status.check.success paymentIntentId=${paymentIntent.id} succeeded=$succeeded" }
        return succeeded
    }

    @Transactional
    fun createPayment(
        request: CheckoutStartRequest,
        paymentIntent: PaymentIntent,
        order: Order,
    ): Payment {
        log.info { "payment.create.started orderId=${order.id} paymentIntentId=${paymentIntent.id}" }
        val payment =
            Payment(
                amount =
                    BigDecimal(paymentIntent.amount)
                        .divide(BigDecimal(100))
                        .setScale(2, RoundingMode.HALF_UP),
                paymentMethod = request.paymentMethod,
                paymentIntentId = paymentIntent.id,
                order = order,
                status = PaymentStatus.PENDING,
            )
        val savedPayment = paymentRepository.save(payment)
        log.info { "payment.create.success orderId=${order.id} paymentIntentId=${paymentIntent.id} amount=${savedPayment.amount}" }
        return savedPayment
    }

    @Transactional
    fun markAsCompleted(paymentIntentId: String): Payment {
        val payment = findPaymentByPaymentIntentId(paymentIntentId)
        payment.status = PaymentStatus.COMPLETED
        return paymentRepository.save(payment)
    }

    @Transactional
    fun findPaymentByPaymentIntentId(paymentIntentId: String): Payment {
        return paymentRepository.findByPaymentIntentId(paymentIntentId)
            ?: throw PaymentNotFoundException("payment intent with id $paymentIntentId not found")
    }
}
