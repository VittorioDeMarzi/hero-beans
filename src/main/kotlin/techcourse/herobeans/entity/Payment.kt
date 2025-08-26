package techcourse.herobeans.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import techcourse.herobeans.enums.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Payment(
    val amount: BigDecimal,
    val currency: String = "eur",
    val paymentMethod: String,
    @ManyToOne(fetch = FetchType.LAZY)
    val order: Order,
    @Column(unique = true)
    val paymentIntentId: String? = null,
    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.PENDING,
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
    @Column(nullable = false)
    var lastUpdatedAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
