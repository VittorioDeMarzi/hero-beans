package techcourse.herobeans.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import techcourse.herobeans.client.StripeClient
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.dto.StartCheckoutRequest
import techcourse.herobeans.exception.PaymentProcessingException
import java.math.BigDecimal
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class PaymentServiceTest {
    @Mock
    private lateinit var stripeClient: StripeClient

    @InjectMocks
    private lateinit var paymentService: PaymentService

    @Test
    fun `should create payment intent successfully`() {
        val request = StartCheckoutRequest(paymentMethod = "card")
        val amount = BigDecimal("29.99")
        val expectedPaymentIntent =
            PaymentIntent(
                id = "pi_test123",
                currency = "eur",
                amount = 2999,
                status = "requires_payment_method",
                memberEmail = "test@example.com",
                clientSecret = "pi_test123_secret",
            )

        whenever(stripeClient.createPaymentIntent(request, amount))
            .thenReturn(expectedPaymentIntent)

        val result = paymentService.createPaymentIntent(request, amount)

        assertThat(result.id).isEqualTo("pi_test123")
        assertThat(result.amount).isEqualTo(2999)
        assertThat(result.clientSecret).isEqualTo("pi_test123_secret")
        verify(stripeClient).createPaymentIntent(request, amount)
    }

    @Test
    fun `should throw exception when payment intent creation fails`() {
        val request = StartCheckoutRequest(paymentMethod = "card")
        val amount = BigDecimal("29.99")

        whenever(stripeClient.createPaymentIntent(request, amount))
            .thenThrow(PaymentProcessingException("Stripe API error"))

        assertThrows<PaymentProcessingException> {
            paymentService.createPaymentIntent(request, amount)
        }
    }

    @Test
    fun `should identify successful payment correctly`() {
        val successfulPayment =
            PaymentIntent(
                id = "pi_test123",
                currency = "eur",
                amount = 2999,
                status = "succeeded",
                memberEmail = "test@example.com",
                clientSecret = "pi_test123_secret",
            )

        val isSucceeded = paymentService.isPaymentSucceeded(successfulPayment)

        assertThat(isSucceeded).isTrue()
    }

    @Test
    fun `should identify failed payment correctly`() {
        val failedPayment =
            PaymentIntent(
                id = "pi_test123",
                currency = "eur",
                amount = 2999,
                status = "requires_payment_method",
                memberEmail = "test@example.com",
                clientSecret = "pi_test123_secret",
            )

        val isSucceeded = paymentService.isPaymentSucceeded(failedPayment)

        assertThat(isSucceeded).isFalse()
    }
}
