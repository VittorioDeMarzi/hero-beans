package techcourse.herobeans.client

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import techcourse.herobeans.config.StripeProperties
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.dto.StartCheckoutRequest
import techcourse.herobeans.exception.StripeProcessingException
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class StripeClient(private val stripeProperties: StripeProperties) {
    private val restClient = RestClient.create()

    fun createPaymentIntent(
        request: StartCheckoutRequest,
        amount: BigDecimal,
    ): PaymentIntent {
        val amountInCents =
            amount.multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toLong()
        val body =
            listOf(
                "amount=$amountInCents",
                "currency=eur",
                "payment_method=${request.paymentMethod}",
                "automatic_payment_methods[enabled]=true",
                "automatic_payment_methods[allow_redirects]=never",
            ).joinToString("&")

        return try {
            val response =
                restClient.post()
                    .uri(stripeProperties.createPaymentIntentUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(PaymentIntent::class.java)

            val paymentIntent = validateResponse(response)

            paymentIntent
        } catch (e: Exception) {
            throw StripeProcessingException("PaymentIntent creation failed", e)
        } // TODO: server 5xx & client 4xx error
    }

    fun confirmPaymentIntent(paymentIntentId: String): PaymentIntent {
        return try {
            val response =
                restClient.post()
                    .uri("/v1/payment_intents/$paymentIntentId/confirm")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .retrieve()
                    .toEntity(PaymentIntent::class.java)

            val paymentIntent = validateResponse(response)

            paymentIntent
        } catch (e: Exception) {
            throw StripeProcessingException("PaymentIntent confirmation failed", e)
        } // TODO: server 5xx & client 4xx error
    }

    private fun validateResponse(response: ResponseEntity<PaymentIntent>): PaymentIntent {
        try {
            val paymentIntent = requireNotNull(response.body)
            require(paymentIntent.id.isNotBlank()) { "Payment intent ID is invalid" }
            require(paymentIntent.status.isNotBlank()) { "Payment intent status is missing" }
            require(paymentIntent.amount > 0) { "Payment amount must be positive: ${paymentIntent.amount}" }
            require(paymentIntent.currency == "eur") { "Unsupported currency:${paymentIntent.currency}" }
            // TODO: "eur" magic string?

            return paymentIntent
        } catch (e: Exception) {
            throw StripeProcessingException("validation: ${e.message}", e)
        }
    }
}
