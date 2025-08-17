package techcourse.herobeans.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.dto.CoffeeDto
import techcourse.herobeans.dto.CoffeePatchRequest
import techcourse.herobeans.dto.CoffeeRequest
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.mapper.CoffeeMapper.toDto
import techcourse.herobeans.mapper.CoffeeMapper.toEntity
import techcourse.herobeans.repository.CoffeeJpaRepository
import kotlin.jvm.optionals.getOrNull

@Service
class CoffeeService(
    private val coffeeJpaRepository: CoffeeJpaRepository,
) {
    @Transactional(readOnly = true)
    fun getAllProducts(): List<CoffeeDto> {
        return coffeeJpaRepository.findAll().map {
            it.toDto()
            // TODO: possible to implement filtering by isAvailable, but showing even unavailable products is also a good strategy
            // TODO: filter by isVisible
        }
    }

    @Transactional(readOnly = true)
    fun getProductById(id: Long): CoffeeDto {
        // TODO: replace exception with NotFoundException!!!
        return coffeeJpaRepository
            .findById(id)
            .orElseThrow { RuntimeException("Coffee with id $id not found") }
            .toDto()
    }

    @Transactional
    fun createCoffee(request: CoffeeRequest): CoffeeDto {
        val coffee = request.toEntity()
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
        patch.description?.let { coffee.description = it?.takeIf { s -> s.isNotBlank() } }
        patch.imageUrl?.let { coffee.imageUrl = it.trim() }

        patch.profile?.let { p ->
            p.body?.let { coffee.profile.body = it }
            p.sweetness?.let { coffee.profile.sweetness = it }
            p.acidity?.let { coffee.profile.acidity = it }
        }

        return coffee.toDto()
    }
}
