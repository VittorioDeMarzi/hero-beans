package techcourse.herobeans.dto

import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel
import java.time.LocalDateTime

class CoffeeDto(
    val isAvailable: Boolean,
    val name: String,
    val profile: ProfileDto,
    val taste: String,
    val brewRecommendation: BrewRecommendation,
    val origin: OriginCountry,
    val processingMethod: ProcessingMethod,
    val options: List<PackageOptionDto> = listOf(),
    val roastLevel: RoastLevel,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
    val description: String? = null,
    val imageUrl: String,
    val id: Long = 0L,
)

class ProfileDto(
    val body: ProfileLevel,
    val sweetness: ProfileLevel,
    val acidity: ProfileLevel,
)
