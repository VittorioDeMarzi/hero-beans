package techcourse.herobeans.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import techcourse.herobeans.entity.Payment

@Repository
interface PaymentJpaRepository : JpaRepository<Payment, Long> {
    fun existsByPaymentIntentId(paymentIntentId: String): Boolean

    fun findByPaymentIntentId(paymentIntentId: String): Payment?
}
