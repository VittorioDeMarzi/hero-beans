package techcourse.herobeans.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import techcourse.herobeans.enums.Grams
import java.math.BigDecimal

class PackageOptionRequest(
    @field:NotNull(message = "Quantity is required")
    @field:Min(0, message = "Quantity must be greater or equal than 0")
    val quantity: Int,
    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than 0")
    val price: BigDecimal,
    @field:NotNull(message = "Weight is required")
    val weight: Grams,
)
