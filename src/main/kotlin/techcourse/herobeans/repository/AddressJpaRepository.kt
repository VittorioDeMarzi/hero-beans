package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import techcourse.herobeans.entity.Address

interface AddressJpaRepository : JpaRepository<Address, Long> {
    fun findAllByMemberId(memberId: Long): List<Address>

    fun findByMemberId(memberId: Long): Address
}
