package techcourse.herobeans.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import techcourse.herobeans.entity.PackageOption

@Repository
interface PackageOptionJpaRepository : JpaRepository<PackageOption, Long> {
    fun findAllByCoffeeId(coffeeId: Long): List<PackageOption>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM PackageOption o WHERE o.id IN :ids ORDER BY o.id")
    fun findByIdsWithLock(ids: List<Long>): List<PackageOption>?
}
