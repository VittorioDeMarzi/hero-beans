package techcourse.herobeans.client

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import techcourse.herobeans.config.StripeProperties
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.dto.StartCheckoutRequest

@Component
class StripeClient(private val stripeProperties: StripeProperties) {
    private val restClient = RestClient.create()

    fun createPaymentIntent(request: StartCheckoutRequest): PaymentIntent {
        val body =
            listOf(
                "amount=${request.amount}",
                "currency=${request.currency}",
                "payment_method=${request.paymentMethod}",
                "automatic_payment_methods[enabled]=true",
                "automatic_payment_methods[allow_redirects]=never",
            ).joinToString("&")

        return try {
            val intent =
                restClient.post()
                    .uri(stripeProperties.createPaymentIntentUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(PaymentIntent::class.java)

            val responseBody = requireNotNull(intent.body)

            responseBody
        } catch (e: Exception) {
            // TODO: implement exception
            throw IllegalArgumentException("PaymentIntent creation failed", e)
        }
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

            val responseBody = requireNotNull(response.body) { "PaymentIntent confirmation failed" }

            responseBody
        } catch (e: Exception) {
            // TODO: implement exception
            throw IllegalArgumentException("PaymentIntent confirmation failed", e)
        }
    }
}
