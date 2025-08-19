package techcourse.herobeans.specification

import org.springframework.data.jpa.domain.Specification
import techcourse.herobeans.dto.CoffeeFilterCriteria
import techcourse.herobeans.entity.Coffee
import techcourse.herobeans.spec.CoffeeSpecs

object SpecificationBuilder {
    fun buildSpecification(criteria: CoffeeFilterCriteria): Specification<Coffee> {
        var spec = Specification.unrestricted<Coffee>()

        criteria.name?.let {
            spec = spec.and(CoffeeSpecs.hasNameContaining(it))
        }
        criteria.brew?.let {
            spec = spec.and(CoffeeSpecs.hasBrewRecommendation(it.filterNotNull().toSet()))
        }
        criteria.originCountry?.let {
            spec = spec.and(CoffeeSpecs.hasOrigin(it))
        }
        criteria.processingMethod?.let {
            spec = spec.and(CoffeeSpecs.hasProcessingMethod(it))
        }
        criteria.roastLevel?.let {
            spec = spec.and(CoffeeSpecs.hasRoastLevel(it))
        }
        criteria.availableOnly.let {
            spec = spec.and(CoffeeSpecs.isAvailable(it))
        }
        return spec
    }
}
