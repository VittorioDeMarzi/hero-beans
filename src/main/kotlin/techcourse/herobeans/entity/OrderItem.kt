package techcourse.herobeans.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class OrderItem(
    @Column(nullable = false)
    val optionId: Long,
    @Column(nullable = false)
    val productName: String,
    @Column(nullable = false)
    val optionName: String,
    @Column(nullable = false)
    val quantity: Int,
    @Column(nullable = false, precision = 12, scale = 2)
    val price: BigDecimal,
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
