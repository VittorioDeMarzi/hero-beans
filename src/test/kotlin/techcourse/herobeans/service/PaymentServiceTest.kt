package techcourse.herobeans.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import techcourse.herobeans.client.StripeClient
import techcourse.herobeans.dto.CheckoutStartRequest
import techcourse.herobeans.dto.FinalizePaymentRequest
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.entity.Address
import techcourse.herobeans.entity.Member
import techcourse.herobeans.enums.MemberRole
import techcourse.herobeans.exception.PaymentIntentNotFoundException
import techcourse.herobeans.exception.PaymentProcessingException
import techcourse.herobeans.repository.PaymentJpaRepository
import java.math.BigDecimal
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
class PaymentServiceTest {
    @Mock
    private lateinit var stripeClient: StripeClient

    @Mock
    private lateinit var paymentRepository: PaymentJpaRepository

    @InjectMocks
    private lateinit var paymentService: PaymentService

    @Test
    fun `should create payment intent successfully`() {
        val member =
            Member(
                name = "Guri Kim",
                email = "guri.kim@herobeans.com",
                password = "very_happy_dog",
                role = MemberRole.USER,
                id = 1L,
            )

        val address =
            Address(
                street = "Oranienburger Str.",
                number = "70",
                city = "Berlin",
                postalCode = "10117",
                countryCode = "DE",
                member = member,
                id = 1L,
            )
        val request = CheckoutStartRequest(paymentMethod = "card", addressId = 1L)
        val amount = BigDecimal("29.99")
        val expectedPaymentIntent =
            PaymentIntent(
                id = PAYMENT_INTENT_ID,
                currency = "eur",
                amount = 2999,
                status = "requires_payment_method",
                clientSecret = PAYMENT_INTENT_CLIENT_SECRET,
            )

        whenever(stripeClient.createPaymentIntent(request, amount))
            .thenReturn(expectedPaymentIntent)

        val result = paymentService.createPaymentIntent(request, amount)

        assertThat(result.id).isEqualTo(PAYMENT_INTENT_ID)
        assertThat(result.amount).isEqualTo(2999)
        assertThat(result.clientSecret).isEqualTo(PAYMENT_INTENT_CLIENT_SECRET)
        verify(stripeClient).createPaymentIntent(request, amount)
    }

    @Test
    fun `should throw exception when payment intent creation fails`() {
        val member =
            Member(
                name = "Guri Kim",
                email = "guri.kim@herobeans.com",
                password = "very_happy_dog",
                role = MemberRole.USER,
                id = 1L,
            )

        val address =
            Address(
                street = "Oranienburger Str.",
                number = "70",
                city = "Berlin",
                postalCode = "10117",
                countryCode = "DE",
                member = member,
                id = 1L,
            )
        val request = CheckoutStartRequest(paymentMethod = "card", addressId = 1L)
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
                id = PAYMENT_INTENT_ID,
                currency = "eur",
                amount = 2999,
                status = "succeeded",
                clientSecret = PAYMENT_INTENT_CLIENT_SECRET,
            )

        val isSucceeded = paymentService.isPaymentSucceeded(successfulPayment)

        assertThat(isSucceeded).isTrue()
    }

    @Test
    fun `should identify failed payment correctly`() {
        val failedPayment =
            PaymentIntent(
                id = PAYMENT_INTENT_ID,
                currency = "eur",
                amount = 2999,
                status = "requires_payment_method",
                clientSecret = PAYMENT_INTENT_CLIENT_SECRET,
            )

        val isSucceeded = paymentService.isPaymentSucceeded(failedPayment)

        assertThat(isSucceeded).isFalse()
    }

    @Test
    fun `should throw NotFoundException when payment intent does not exist in repository`() {
        val request = FinalizePaymentRequest(paymentIntentId = "pi_missing", orderId = 1L)

        whenever(paymentRepository.existsByPaymentIntentId("pi_missing")).thenReturn(false)

        assertThrows<PaymentIntentNotFoundException> {
            paymentService.confirmPaymentIntent(request.paymentIntentId)
        }
    }

    @Test
    fun `should confirm payment intent successfully when exists in repository`() {
        val request = FinalizePaymentRequest(paymentIntentId = PAYMENT_INTENT_ID, orderId = 1L)
        val expectedPaymentIntent =
            PaymentIntent(
                id = PAYMENT_INTENT_ID,
                currency = "eur",
                amount = 2999,
                status = "succeeded",
                clientSecret = PAYMENT_INTENT_CLIENT_SECRET,
            )

        whenever(paymentRepository.existsByPaymentIntentId(PAYMENT_INTENT_ID)).thenReturn(true)
        whenever(stripeClient.confirmPaymentIntent(PAYMENT_INTENT_ID)).thenReturn(expectedPaymentIntent)

        val result = paymentService.confirmPaymentIntent(request.paymentIntentId)

        assertThat(result).isEqualTo(expectedPaymentIntent)
        verify(stripeClient).confirmPaymentIntent(PAYMENT_INTENT_ID)
    }

    companion object {
        const val PAYMENT_INTENT_ID = "pi_test_guri_123"
        const val PAYMENT_INTENT_CLIENT_SECRET = "pi_test_guri_123_secret"
    }
}
