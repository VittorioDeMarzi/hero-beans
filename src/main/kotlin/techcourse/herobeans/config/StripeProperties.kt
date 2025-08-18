package techcourse.herobeans.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("stripe")
class StripeProperties(
    val secretKey: String,
    val createPaymentIntentUrl: String,
)
