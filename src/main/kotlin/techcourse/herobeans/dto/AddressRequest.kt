package techcourse.herobeans.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

class AddressRequest(
    @field:NotBlank(message = "Street is required")
    val street: String,
    @field:NotBlank(message = "Number is required")
    val number: String,
    @field:NotBlank(message = "City is required")
    val city: String = "Berlin",
    @field:NotBlank(message = "Postal code is required")
    @field:Pattern(regexp = "\\d{5}", message = "Postal code must be 5 digits")
    val postalCode: String,
    @field:NotBlank(message = "Country is required")
    val countryCode: String = "DE",
    @field:Size(max = 20, message = "Label can be at most 20 characters")
    val label: String? = null,
    val id: Long = 0L,
)
