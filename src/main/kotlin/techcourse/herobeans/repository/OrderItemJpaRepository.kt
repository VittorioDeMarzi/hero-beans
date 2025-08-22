package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import techcourse.herobeans.entity.OrderItem

interface OrderItemJpaRepository : JpaRepository<OrderItem, Long>
