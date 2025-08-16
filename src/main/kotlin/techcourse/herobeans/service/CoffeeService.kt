package techcourse.herobeans.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.dto.CoffeeDto
import techcourse.herobeans.mapper.CoffeeMapper.toDto
import techcourse.herobeans.repository.CoffeeJpaRepository

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
}
