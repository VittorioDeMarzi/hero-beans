package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import techcourse.herobeans.entity.Cart

interface CartJpaRepository : JpaRepository<Cart, Long> {
    fun findByMemberId(memberId: Long): Cart?
}
