package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import techcourse.herobeans.entity.Order
import java.util.Optional

interface OrderJpaRepository : JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    fun findByIdWithOrderItems(id: Long): Optional<Order>
}
