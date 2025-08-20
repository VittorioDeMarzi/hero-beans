package techcourse.herobeans.dto

import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.RoastLevel

data class CoffeeFilterCriteria(
    val name: String? = null,
    val brew: Set<BrewRecommendation?>? = null,
    val originCountry: OriginCountry? = null,
    val processingMethod: ProcessingMethod? = null,
    val roastLevel: RoastLevel? = null,
    val availableOnly: Boolean = false,
)
