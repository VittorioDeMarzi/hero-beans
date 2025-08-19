package techcourse.herobeans.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.dto.CoffeeDto
import techcourse.herobeans.dto.CoffeeFilterCriteria
import techcourse.herobeans.dto.CoffeePatchRequest
import techcourse.herobeans.dto.CoffeeRequest
import techcourse.herobeans.dto.PackageOptionRequest
import techcourse.herobeans.enums.CoffeeSorting
import techcourse.herobeans.enums.toSpringSort
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.mapper.CoffeeMapper.toDto
import techcourse.herobeans.mapper.CoffeeMapper.toEntity
import techcourse.herobeans.repository.CoffeeJpaRepository
import techcourse.herobeans.repository.requireDoesNotExistByName
import techcourse.herobeans.specification.SpecificationBuilder
import kotlin.jvm.optionals.getOrNull

@Service
class CoffeeService(
    private val coffeeJpaRepository: CoffeeJpaRepository,
) {
    @Transactional(readOnly = true)
    fun getAllProducts(
        page: Int,
        size: Int,
        filterCriteria: CoffeeFilterCriteria,
        sort: CoffeeSorting,
    ): Page<CoffeeDto> {
        val sort = toSpringSort(sort)
        val pageable = PageRequest.of(page, size, sort)
        val spec = SpecificationBuilder.buildSpecification(filterCriteria)
        return coffeeJpaRepository.findAll(spec, pageable).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getProductById(id: Long): CoffeeDto {
        return coffeeJpaRepository
            .findById(id)
            .orElseThrow { NotFoundException("Coffee with id $id not found") }
            .toDto()
    }

    @Transactional
    fun createCoffee(request: CoffeeRequest): CoffeeDto {
        val coffee = request.toEntity()
        coffeeJpaRepository.requireDoesNotExistByName(coffee.name)
        val saved = coffeeJpaRepository.save(coffee)
        return saved.toDto()
    }

    @Transactional
    fun deleteProduct(id: Long) {
        coffeeJpaRepository.findById(id).getOrNull() ?: throw NotFoundException("Product with id $id not found")
        coffeeJpaRepository.deleteById(id)
    }

    @Transactional
    fun updateProduct(
        id: Long,
        patch: CoffeePatchRequest,
    ): CoffeeDto {
        val coffee = coffeeJpaRepository.findById(id).getOrNull() ?: throw NotFoundException("Product with id $id not found")
        patch.name?.let { coffee.name = it.trim() }
        patch.taste?.let { coffee.taste = it.trim() }
        patch.brewRecommendation?.let { coffee.brewRecommendation = it }
        patch.origin?.let { coffee.origin = it }
        patch.processingMethod?.let { coffee.processingMethod = it }
        patch.roastLevel?.let { coffee.roastLevel = it }
        patch.description?.let { coffee.description = it }
        patch.imageUrl?.let { coffee.imageUrl = it.trim() }
        patch.certificates?.let { coffee.certificates = it.toMutableList() }

        patch.profile?.let { p ->
            p.body?.let { coffee.profile.body = it }
            p.sweetness?.let { coffee.profile.sweetness = it }
            p.acidity?.let { coffee.profile.acidity = it }
        }

        return coffee.toDto()
    }

    @Transactional
    fun addOptionToCoffee(
        id: Long,
        option: PackageOptionRequest,
    ): CoffeeDto {
        val coffee = coffeeJpaRepository.findById(id).getOrNull() ?: throw NotFoundException("Product with id $id not found")
        coffee.addOption(option.toEntity())
        return coffee.toDto()
    }
}
