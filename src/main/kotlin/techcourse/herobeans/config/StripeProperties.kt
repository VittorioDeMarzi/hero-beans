package techcourse.herobeans.config

import org.springframework.boot.context.properties.ConfigurationProperties

// TODO: delete this default value
@ConfigurationProperties("stripe")
class StripeProperties(
    val secretKey: String,
    val paymentIntentUrl: String,
)
