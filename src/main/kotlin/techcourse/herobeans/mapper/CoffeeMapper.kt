package techcourse.herobeans.mapper

import techcourse.herobeans.dto.CoffeeDto
import techcourse.herobeans.dto.CoffeeRequest
import techcourse.herobeans.dto.PackageOptionDto
import techcourse.herobeans.dto.PackageOptionRequest
import techcourse.herobeans.dto.ProfileDto
import techcourse.herobeans.entity.Coffee
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.entity.Profile

object CoffeeMapper {
    fun Coffee.toDto(): CoffeeDto {
        val profile = this.profile.toDto()
        val options = this.options.map { it.toDto() }
        return CoffeeDto(
            isAvailable = this.isAvailable,
            name = this.name,
            profile = profile,
            taste = this.taste,
            brewRecommendation = this.brewRecommendation,
            origin = this.origin,
            processingMethod = this.processingMethod,
            options = options,
            roastLevel = this.roastLevel,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            description = this.description,
            imageUrl = this.imageUrl,
            id = this.id,
        )
    }

    fun Profile.toDto(): ProfileDto {
        return ProfileDto(
            body = this.body,
            sweetness = this.sweetness,
            acidity = this.acidity,
        )
    }

    fun PackageOption.toDto(): PackageOptionDto {
        return PackageOptionDto(
            stockStatus = this.stockStatus,
            quantity = this.quantity,
            price = this.price,
            weight = this.weight,
            id = this.id,
        )
    }

    fun CoffeeRequest.toEntity(): Coffee {
        val coffee =
            Coffee(
                name = name,
                profile =
                    Profile(
                        body = profile.body,
                        sweetness = profile.sweetness,
                        acidity = profile.acidity,
                    ),
                taste = taste,
                brewRecommendation = brewRecommendation,
                origin = origin,
                processingMethod = processingMethod,
                roastLevel = roastLevel,
                description = description,
                imageUrl = imageUrl,
                options = mutableListOf(),
                id = 0L,
            )

        this.options.forEach {
            val option = it.toEntity()
            coffee.addOption(option)
        }

        return coffee
    }

    fun PackageOptionRequest.toEntity(): PackageOption {
        return PackageOption(
            quantity = this.quantity,
            price = this.price,
            weight = this.weight,
        )
    }
}
