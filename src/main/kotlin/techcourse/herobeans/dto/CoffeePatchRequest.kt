package techcourse.herobeans.dto

import jakarta.validation.Valid
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel

class CoffeePatchRequest(
    val name: String? = null,
    val taste: String? = null,
    val brewRecommendation: BrewRecommendation? = null,
    val origin: OriginCountry? = null,
    val processingMethod: ProcessingMethod? = null,
    val roastLevel: RoastLevel? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    @field:Valid
    val profile: ProfilePatchRequest? = null,
)

class ProfilePatchRequest(
    val body: ProfileLevel? = null,
    val sweetness: ProfileLevel? = null,
    val acidity: ProfileLevel? = null,
)
