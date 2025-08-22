package techcourse.herobeans.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.repository.PackageOptionJpaRepository

@Service
class PackageOptionService(
    private val optionRepository: PackageOptionJpaRepository,
) {
    fun findByIdsWithLock(ids: List<Long>): List<PackageOption> {
        return optionRepository.findByIdsWithLock(ids)
            ?: throw NotFoundException("Option IDs don't match between requested locked options")
    }

    @Transactional
    fun saveAll(options: List<PackageOption>): List<PackageOption> {
        return optionRepository.saveAll(options)
    }
}
