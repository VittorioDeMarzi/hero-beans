package techcourse.herobeans.config

import org.springframework.boot.context.properties.ConfigurationProperties

// TODO: delete this default value
@ConfigurationProperties("stripe")
class StripeProperties(
    val secretKey: String = "sk_test_default",
    val createPaymentIntentUrl: String = "https://api.stripe.com",
)
