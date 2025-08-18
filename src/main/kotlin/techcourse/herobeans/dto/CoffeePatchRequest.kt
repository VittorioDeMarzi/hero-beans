package techcourse.herobeans.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel

class CoffeePatchRequest(
    @field:Size(max = 100, message = "Name must be at most 100 characters")
    val name: String? = null,
    val taste: String? = null,
    val brewRecommendation: BrewRecommendation? = null,
    val origin: OriginCountry? = null,
    val processingMethod: ProcessingMethod? = null,
    val roastLevel: RoastLevel? = null,
    @field:Size(max = 200, message = "Name must be at most 200 characters")
    val description: String? = null,
    @field:Pattern(regexp = "^https?://.+", message = "Image URL must start with http or https")
    val imageUrl: String? = null,
    @field:Valid
    val profile: ProfilePatchRequest? = null,
)

class ProfilePatchRequest(
    val body: ProfileLevel? = null,
    val sweetness: ProfileLevel? = null,
    val acidity: ProfileLevel? = null,
)
