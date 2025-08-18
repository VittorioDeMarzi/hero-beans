package techcourse.herobeans.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
class CartItem(
    @ManyToOne
    @JoinColumn(name = "cart_id")
    var cart: Cart?,
    @ManyToOne
    @JoinColumn(name = "package_option_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    val option: PackageOption,
    var quantity: Int,
    val priceSnapshot: BigDecimal = option.price,
    @CreatedDate
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    var lastUpdatedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
