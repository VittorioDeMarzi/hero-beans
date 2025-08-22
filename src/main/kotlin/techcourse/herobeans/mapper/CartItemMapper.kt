package techcourse.herobeans.mapper

import techcourse.herobeans.entity.CartItem
import techcourse.herobeans.entity.OrderItem

object CartItemMapper {
    fun List<CartItem>.toOrderItems(): List<OrderItem> {
        return this.map { it.toOrderItem() }
    }

    fun CartItem.toOrderItem(): OrderItem {
        return OrderItem(
            optionId = this.option.id,
            productName = this.option.coffee!!.name,
            optionName = this.option.weight.name,
            quantity = this.quantity,
            price = priceSnapshot,
        )
    }
}
