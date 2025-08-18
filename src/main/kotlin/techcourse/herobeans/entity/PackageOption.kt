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
import techcourse.herobeans.enums.StockStatus
import techcourse.herobeans.exception.InsufficientStockException
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
    val id: Long = 0L,
) {
    init {
        require(quantity in MIN_QUANTITY..MAX_QUANTITY) { "quantity must be between $MIN_QUANTITY..$MAX_QUANTITY" }
        require(price >= MIN_PRICE.toBigDecimal()) { "price must be greater than " }
    }

    val stockStatus: StockStatus
        get() =
            when (quantity) {
                0 -> StockStatus.OUT_OF_STOCK
                in 1..20 -> StockStatus.LOW_STOCK
                else -> StockStatus.IN_STOCK
            }

    fun increaseQuantity(plusQuantity: Int): PackageOption {
        require(quantity >= 0) { "quantity must be positive" }
        require(quantity + plusQuantity <= MAX_QUANTITY) { "quantity must be between $MIN_QUANTITY and $MAX_QUANTITY" }
        quantity += plusQuantity
        return this
    }

    fun decreaseQuantity(minusQuantity: Int): PackageOption {
        require(quantity >= 0) { "quantity must be positive" }
        require(quantity - minusQuantity <= MIN_QUANTITY) { "quantity must be between $MIN_QUANTITY and $MAX_QUANTITY" }
        quantity -= minusQuantity
        return this
    }

    fun checkAvailabilityInStock(value: Int) {
        if (quantity < value) {
            throw InsufficientStockException("Insufficient quantity of ${coffee?.name} ${weight.name}. Items available in stock: $quantity")
        }
    }

    companion object {
        private const val MIN_PRICE = 0.5
        private const val MIN_QUANTITY = 0
        private const val MAX_QUANTITY = 10_000
    }
}
