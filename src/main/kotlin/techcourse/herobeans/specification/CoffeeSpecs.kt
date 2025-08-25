package techcourse.herobeans.specification

import org.springframework.data.jpa.domain.Specification
import techcourse.herobeans.entity.Coffee
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.RoastLevel

object CoffeeSpecs {
    fun hasNameContaining(name: String): Specification<Coffee> {
        return Specification { root, _, cb ->
            cb.like(cb.lower(root.get("name")), "%${name.lowercase()}%")
        }
    }

    fun hasOrigin(origin: OriginCountry): Specification<Coffee> {
        return Specification { root, _, cb ->
            cb.equal(root.get<OriginCountry>("origin"), origin)
        }
    }

    fun hasBrewRecommendation(brewRecs: Set<BrewRecommendation>): Specification<Coffee> {
        return Specification { root, _, cb ->
            root.get<BrewRecommendation>("brewRecommendation").`in`(brewRecs)
        }
    }

    fun hasProcessingMethod(processingMethod: ProcessingMethod): Specification<Coffee> {
        return Specification { root, _, cb ->
            cb.equal(root.get<ProcessingMethod>("processingMethod"), processingMethod)
        }
    }

    fun hasRoastLevel(roastLevel: RoastLevel): Specification<Coffee> {
        return Specification { root, _, cb ->
            cb.equal(root.get<RoastLevel>("roastLevel"), roastLevel)
        }
    }

    fun isAvailable(availableOnly: Boolean): Specification<Coffee> {
        return Specification { root, _, cb ->
            if (availableOnly) {
                cb.isTrue(root.get("isAvailable"))
            } else {
                cb.conjunction()
            }
        }
    }
}
