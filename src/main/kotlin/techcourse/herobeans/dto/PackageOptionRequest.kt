package techcourse.herobeans.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import techcourse.herobeans.enums.Grams
import java.math.BigDecimal

class PackageOptionRequest(
    @field:NotNull(message = "Quantity is required")
    @field:Positive(message = "Quantity must be greater than 0")
    val quantity: Int,
    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than 0")
    val price: BigDecimal,
    @field:NotNull(message = "Weight is required")
    val weight: Grams,
)
