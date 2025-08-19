package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import techcourse.herobeans.entity.Coffee
import techcourse.herobeans.exception.CoffeeNameAlreadyExistsException

interface CoffeeJpaRepository : JpaRepository<Coffee, Long>, JpaSpecificationExecutor<Coffee> {
    fun existsByName(name: String): Boolean
}

fun CoffeeJpaRepository.requireDoesNotExistByName(name: String) {
    if (existsByName(name)) {
        throw CoffeeNameAlreadyExistsException("Coffee with name '$name' already exists.")
    }
}
