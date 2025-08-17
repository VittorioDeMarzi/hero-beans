package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import techcourse.herobeans.entity.PackageOption

@Repository
interface PackageOptionJpaRepository : JpaRepository<PackageOption, Long> {
    fun findAllByCoffeeId(coffeeId: Long): List<PackageOption>
}
