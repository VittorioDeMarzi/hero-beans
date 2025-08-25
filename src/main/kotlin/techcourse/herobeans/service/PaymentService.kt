package techcourse.herobeans.service

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
        val paymentIntent = stripeClient.createPaymentIntent(request, totalAmount)
        return paymentIntent
    }

    fun confirmPaymentIntent(paymentIntentId: String): PaymentIntent {
        require(paymentRepository.existsByPaymentIntentId(paymentIntentId)) {
            throw PaymentIntentNotFoundException("payment intent with id $paymentIntentId not found")
        }
        val paymentIntent = stripeClient.confirmPaymentIntent(paymentIntentId)
        return paymentIntent
    }

    fun isPaymentSucceeded(paymentIntent: PaymentIntent): Boolean {
        return paymentIntent.status == "succeeded"
    }

    @Transactional
    fun createPayment(
        request: CheckoutStartRequest,
        paymentIntent: PaymentIntent,
        order: Order,
    ): Payment {
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
        return paymentRepository.save(payment)
    }

    @Transactional
    fun markAsCompleted(paymentIntentId: String): Payment {
        val payment = findPaymentByPaymentIntentId(paymentIntentId)
        payment.status = PaymentStatus.COMPLETED
        return paymentRepository.save(payment)
    }

    @Transactional
    fun markAsFailed(paymentIntentId: String): Payment {
        val payment = findPaymentByPaymentIntentId(paymentIntentId)
        payment.status = PaymentStatus.FAILED
        return paymentRepository.save(payment)
    }

    @Transactional
    fun findPaymentByPaymentIntentId(paymentIntentId: String): Payment {
        return paymentRepository.findByPaymentIntentId(paymentIntentId)
            ?: throw PaymentNotFoundException("payment intent with id $paymentIntentId not found")
    }
}
