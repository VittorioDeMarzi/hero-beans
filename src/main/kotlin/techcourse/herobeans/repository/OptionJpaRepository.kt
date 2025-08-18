package techcourse.herobeans.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import techcourse.herobeans.entity.PackageOption

interface OptionJpaRepository : JpaRepository<PackageOption, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM PackageOption o WHERE o.id IN :ids ORDER BY o.id")
    fun findByIdsWithLock(ids: List<Long>): List<PackageOption>?
}
