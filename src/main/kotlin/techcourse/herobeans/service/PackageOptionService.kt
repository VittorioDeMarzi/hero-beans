package techcourse.herobeans.service

import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.repository.PackageOptionJpaRepository

private val log = KotlinLogging.logger {}

@Service
class PackageOptionService(
    private val optionRepository: PackageOptionJpaRepository,
) {
    fun findByIdsWithLock(ids: List<Long>): List<PackageOption> {
        log.info { "option.lock.started optionIds=$ids" }
        val options =
            optionRepository.findByIdsWithLock(ids)
                ?: throw NotFoundException("Option IDs don't match between requested locked options")
        log.info { "option.lock.success optionIds=$ids count=${options.size}" }
        return options
    }

    @Transactional
    fun saveAll(options: List<PackageOption>): List<PackageOption> {
        log.info { "option.save.started optionIds=${options.map { it.id }}" }
        val savedOptions = optionRepository.saveAll(options)
        log.info { "option.save.success optionIds=${savedOptions.map { it.id }} count=${savedOptions.size}" }
        return savedOptions
    }
}
