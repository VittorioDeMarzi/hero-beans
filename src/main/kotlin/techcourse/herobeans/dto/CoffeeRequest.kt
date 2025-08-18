package techcourse.herobeans.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.Certificate
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel

data class CoffeeRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100, message = "Name must be at most 100 characters")
    val name: String,
    @field:NotBlank(message = "Taste is required")
    val taste: String,
    @field:NotNull(message = "Brew recommendation is required")
    val brewRecommendation: BrewRecommendation,
    @field:NotNull(message = "Origin country is required")
    val origin: OriginCountry,
    @field:NotNull(message = "Processing method is required")
    val processingMethod: ProcessingMethod,
    @field:NotNull(message = "Roast level is required")
    val roastLevel: RoastLevel,
    val certificates: List<Certificate> = listOf(),
    @field:NotBlank(message = "Description is required")
    @field:Size(max = 200, message = "Name must be at most 200 characters")
    val description: String,
    @field:NotBlank(message = "Image URL is required")
    @field:Pattern(regexp = "^https?://.+", message = "Image URL must start with http or https")
    val imageUrl: String,
    @field:Valid
    @field:NotNull(message = "Profile is required")
    val profile: ProfileRequest,
    @field:Valid
    @field:NotEmpty(message = "At least one package option is required")
    val options: List<PackageOptionRequest>,
)

data class ProfileRequest(
    @field:NotNull val body: ProfileLevel,
    @field:NotNull val sweetness: ProfileLevel,
    @field:NotNull val acidity: ProfileLevel,
)
