package techcourse.herobeans.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import techcourse.herobeans.enums.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
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
    @CreationTimestamp
    var createdAt: LocalDateTime? = null,
    @UpdateTimestamp
    var lastUpdatedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
