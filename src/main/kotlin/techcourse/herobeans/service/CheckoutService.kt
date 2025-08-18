package techcourse.herobeans.service

import org.springframework.stereotype.Service
import techcourse.herobeans.client.StripeClient
import techcourse.herobeans.dto.FinalizePaymentRequest
import techcourse.herobeans.dto.FinalizePaymentResponse
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.dto.StartCheckoutRequest
import techcourse.herobeans.dto.StartCheckoutResponse
import techcourse.herobeans.repository.OrderJpaRepository

@Service
class CheckoutService(
    private val stripeClient: StripeClient,
    private val orderRepository: OrderJpaRepository,
) {
    fun startOrder(
        member: MemberDto,
        request: StartCheckoutRequest,
    ): StartCheckoutResponse {
        // TODO: implement this method
        val paymentIntent = createPaymentIntent(request)
        return StartCheckoutResponse(paymentIntent.id)
    }

    private fun createPaymentIntent(request: StartCheckoutRequest): PaymentIntent {
        // TODO: implement this method
        return stripeClient.createPaymentIntent(request)
    }

    fun finalizeOrder(
        orderId: Long,
        request: FinalizePaymentRequest,
    ): FinalizePaymentResponse {
        // TODO: implement this method
        return FinalizePaymentResponse()
    }
}
