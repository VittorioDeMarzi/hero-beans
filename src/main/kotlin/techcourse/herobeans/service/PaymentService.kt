package techcourse.herobeans.service

import org.springframework.stereotype.Service
import techcourse.herobeans.client.StripeClient
import techcourse.herobeans.dto.FinalizePaymentRequest
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.dto.StartCheckoutRequest
import techcourse.herobeans.entity.Order
import techcourse.herobeans.entity.Payment
import techcourse.herobeans.enums.PaymentStatus
import techcourse.herobeans.repository.PaymentJpaRepository
import java.math.BigDecimal

@Service
class PaymentService(
    private val stripeClient: StripeClient,
    private val paymentRepository: PaymentJpaRepository,
) {
    fun createPaymentIntent(
        request: StartCheckoutRequest,
        totalAmount: BigDecimal,
    ): PaymentIntent {
        val paymentIntent = stripeClient.createPaymentIntent(request, totalAmount)
        return paymentIntent
    }

    fun confirmPaymentIntent(request: FinalizePaymentRequest): PaymentIntent {
        val paymentIntent = stripeClient.confirmPaymentIntent(request.paymentIntentId)
        return paymentIntent
    }

    fun isPaymentSucceeded(paymentIntent: PaymentIntent): Boolean {
        return paymentIntent.status == "succeeded"
    }

    fun createPayment(
        request: StartCheckoutRequest,
        paymentIntent: PaymentIntent,
        order: Order,
    ): Payment {
        val payment =
            Payment(
                amount = BigDecimal(paymentIntent.amount),
                paymentMethod = request.paymentMethod,
                paymentIntentId = paymentIntent.id,
                order = order,
                status = PaymentStatus.PENDING,
            )

        return paymentRepository.save(payment)
    }
}
