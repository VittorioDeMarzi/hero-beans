package techcourse.herobeans.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("stripe")
class StripeProperties(
    val secretKey: String,
    val paymentIntentUrl: String,
)
