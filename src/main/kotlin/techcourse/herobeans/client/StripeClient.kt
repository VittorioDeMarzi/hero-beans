package techcourse.herobeans.client

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import techcourse.herobeans.config.StripeProperties
import techcourse.herobeans.dto.CheckoutStartRequest
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.exception.StripeClientException
import techcourse.herobeans.exception.StripeProcessingException
import techcourse.herobeans.exception.StripeServerException
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class StripeClient(private val stripeProperties: StripeProperties) {

    private val restClient = RestClient.create()

    companion object {
        private const val CURRENCY_EUR = "eur"
        private const val CENTS_MULTIPLIER = 100
    }

    fun createPaymentIntent(
        request: CheckoutStartRequest,
        amount: BigDecimal,
    ): PaymentIntent {
        val amountInCents = convertToCents(amount)
        val body = buildPaymentIntentBody(amountInCents, request.paymentMethod)

        return executeStripeRequest {
            restClient.post()
                .uri(stripeProperties.paymentIntentUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toEntity(PaymentIntent::class.java)
        }
    }

    fun confirmPaymentIntent(paymentIntentId: String): PaymentIntent {
        return executeStripeRequest {
            restClient.post()
                .uri("${stripeProperties.paymentIntentUrl}/$paymentIntentId/confirm")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .toEntity(PaymentIntent::class.java)
        }
    }

    private fun convertToCents(amount: BigDecimal): Long {
        return amount.multiply(BigDecimal(CENTS_MULTIPLIER))
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()
    }

    private fun buildPaymentIntentBody(amountInCents: Long, paymentMethod: String): String {
        return listOf(
            "amount=$amountInCents",
            "currency=$CURRENCY_EUR",
            "payment_method=$paymentMethod",
            "automatic_payment_methods[enabled]=true",
            "automatic_payment_methods[allow_redirects]=never",
        ).joinToString("&")
    }
    private fun executeStripeRequest(request: () -> ResponseEntity<PaymentIntent>): PaymentIntent {
        return try {
            val response = request()
            validateResponse(response)
        } catch (e: HttpClientErrorException) {
            throw StripeClientException(
                "Stripe client error: status=${e.statusCode.value()}, message=${e.message}",
                e
            )
        } catch (e: HttpServerErrorException) {
            throw StripeServerException(
                "Stripe server error: status=${e.statusCode.value()}, message=${e.message}",
                e
            )
        } catch (e: Exception) {
            throw StripeProcessingException("Stripe processing failed: ${e.message}", e)
        }
    }


    private fun validateResponse(response: ResponseEntity<PaymentIntent>): PaymentIntent {
        try {
            val paymentIntent = requireNotNull(response.body)
            require(paymentIntent.id.isNotBlank()) { "Payment intent ID is invalid" }
            require(paymentIntent.status.isNotBlank()) { "Payment intent status is missing" }
            require(paymentIntent.amount > 0) { "Payment amount must be positive: ${paymentIntent.amount}" }
            require(paymentIntent.currency == "eur") { "Unsupported currency:${paymentIntent.currency}" }

            return paymentIntent
        } catch (e: Exception) {
            throw IllegalArgumentException() //TODO: 422
        }
    }
}
