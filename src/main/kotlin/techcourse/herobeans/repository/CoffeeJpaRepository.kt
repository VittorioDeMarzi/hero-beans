package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import techcourse.herobeans.entity.Coffee

interface CoffeeJpaRepository : JpaRepository<Coffee, Long>
