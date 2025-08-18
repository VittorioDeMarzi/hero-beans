package techcourse.herobeans.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.sumOf

@Entity
class Cart(
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    val member: Member,
    @OneToMany(
        cascade = [CascadeType.ALL],
        mappedBy = "cart",
        orphanRemoval = true,
    )
    private val _items: MutableList<CartItem> = mutableListOf(),
    @CreatedDate
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    var lastUpdatedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    val totalAmount: BigDecimal
        get() = _items.sumOf { it.option.price.multiply(BigDecimal(it.quantity)) }

    val items: List<CartItem>
        get() = _items.toList()

    fun addOrIncrement(item: CartItem) {
        require(item.quantity > 0) { IllegalArgumentException("Quantity must be > 0") }
        item.option.checkAvailabilityInStock(item.quantity)
        val existing = _items.firstOrNull { it.option.id == item.option.id }
        if (existing == null) {
            item.option.checkAvailabilityInStock(item.quantity)
            item.cart = this
            _items.add(item)
        } else {
            val newQty = existing.quantity + item.quantity
            existing.option.checkAvailabilityInStock(newQty)
            existing.quantity = newQty
        }
    }

    fun removeItem(optionId: Long) {
        val it = _items.firstOrNull { it.option.id == optionId } ?: return
        _items.remove(it)
        it.cart = null
    }

    fun clear() {
        _items.forEach { it.cart = null }
        _items.clear()
    }
}
