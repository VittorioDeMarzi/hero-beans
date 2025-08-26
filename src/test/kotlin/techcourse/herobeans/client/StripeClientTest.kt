package techcourse.herobeans.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import techcourse.herobeans.dto.CheckoutStartRequest
import techcourse.herobeans.entity.Member
import techcourse.herobeans.enums.MemberRole
import java.math.BigDecimal

@Disabled("Stripe integration test - requires valid API key")
@SpringBootTest(
    properties = [
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=none",
    ],
)
@ActiveProfiles("test")
class StripeClientTest {
    @Autowired
    private lateinit var stripeClient: StripeClient

    @Test
    fun createPaymentIntent() {
        val guri =
            Member(
                name = "Guri Kim",
                email = "guri.kim@herobeans.com",
                password = "guri_paw_smell!",
                role = MemberRole.USER,
                id = 1L,
            )
        val request =
            CheckoutStartRequest(
                paymentMethod = "pm_card_visa",
                addressId = 1L,
            )

        val amount = BigDecimal(10)
        val actual = stripeClient.createPaymentIntent(request, amount)

        assertThat(actual).isNotNull
        assertThat(actual.amount).isEqualTo(1000)
    }

    @Test
    fun confirmPaymentIntent() {
        val guri =
            Member(
                name = "Guri Kim",
                email = "guri.kim@herobeans.com",
                password = "guri_paw_smell!",
                role = MemberRole.USER,
                id = 1L,
            )
        val request =
            CheckoutStartRequest(
                paymentMethod = "pm_card_visa",
                addressId = 1L,
            )

        val amount = BigDecimal(10)
        val createdPaymentIntent = stripeClient.createPaymentIntent(request, amount)

        val actual = stripeClient.confirmPaymentIntent(createdPaymentIntent.id)

        assertThat(actual).isNotNull
        assertThat(actual.id).isEqualTo(createdPaymentIntent.id)
        assertThat(actual.status).isIn("requires_action", "processing", "succeeded")
        assertThat(actual.amount).isEqualTo(1000)
        assertThat(actual.currency).isEqualTo("eur")
    }
}
