package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import techcourse.herobeans.entity.Member

interface MemberJpaRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?

    fun existsByEmail(email: String): Boolean
}
