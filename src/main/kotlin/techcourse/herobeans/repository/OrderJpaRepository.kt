package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import techcourse.herobeans.entity.Order

interface OrderJpaRepository : JpaRepository<Order, Long>
