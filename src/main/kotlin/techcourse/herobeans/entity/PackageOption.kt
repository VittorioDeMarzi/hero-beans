package techcourse.herobeans.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import techcourse.herobeans.enums.Grams
import java.math.BigDecimal

@Entity
class PackageOption(
    @Column(nullable = false)
    var quantity: Int,
    @Column(nullable = false)
    var price: BigDecimal,
    @Column(nullable = false)
    val weight: Grams,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffee_id")
    var coffee: Coffee? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    init {
        require(quantity in MIN_QUANTITY..MAX_QUANTITY) { "quantity must be between $MIN_QUANTITY..$MAX_QUANTITY" }
        require(price >= MIN_PRICE.toBigDecimal()) { "price must be greater than " }
    }

    val stockStatus: String
        get() =
            when (quantity) {
                0 -> "out of stock" // TODO: return Enum??
                in 1..10 -> "low stock"
                else -> ""
            }

    fun increaseQuantity(plusQuantity: Int) {
        require(quantity >= 0) { "quantity must be positive" }
        require(quantity + plusQuantity <= MAX_QUANTITY) { "quantity must be between $MIN_QUANTITY and $MAX_QUANTITY" }
        quantity += plusQuantity
    }

    fun decreaseQuantity(minusQuantity: Int) {
        require(quantity >= 0) { "quantity must be positive" }
        require(quantity - minusQuantity <= MIN_QUANTITY) { "quantity must be between $MIN_QUANTITY and $MAX_QUANTITY" }
        quantity -= minusQuantity
    }

    companion object {
        private const val MIN_PRICE = 0.5
        private const val MIN_QUANTITY = 0
        private const val MAX_QUANTITY = 10_000
    }
}
