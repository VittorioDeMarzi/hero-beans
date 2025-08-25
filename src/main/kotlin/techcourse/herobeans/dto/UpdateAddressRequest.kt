package techcourse.herobeans.dto

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

class UpdateAddressRequest(
    val street: String? = null,
    val number: String? = null,
    @field:Pattern(regexp = "\\d{5}", message = "Postal code must be 5 digits")
    val postalCode: String? = null,
    @field:Size(max = 20, message = "Label can be at most 20 characters")
    val label: String? = null,
)
