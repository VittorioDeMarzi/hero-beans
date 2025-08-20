package techcourse.herobeans.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class PaymentIntent(
    val id: String,
    val amount: Int,
    val status: String,
    @JsonProperty("client_secret")
    val clientSecret: String,
    val currency: String,
    @JsonProperty("object")
    val objectType: String? = null,
)