package techcourse.herobeans.mapper

import techcourse.herobeans.dto.CartProductItem
import techcourse.herobeans.dto.CartProductResponse
import techcourse.herobeans.entity.Cart
import techcourse.herobeans.entity.CartItem
import java.math.BigDecimal

object CartMapper {
    fun toResponse(cart: Cart): CartProductResponse {
        val items = cart.items.map { toItem(it) }
        return CartProductResponse(
            items = items,
            totalAmount = cart.totalAmount,
        )
    }

    fun toItem(ci: CartItem): CartProductItem {
        val unitPrice = ci.option.price
        return CartProductItem(
            cartItemId = ci.id,
            optionId = ci.option.id,
            productName = ci.option.coffee?.name ?: "Unknown",
            optionName = ci.option.weight.name,
            imageUrl = ci.option.coffee?.imageUrl,
            unitPrice = unitPrice,
            quantity = ci.quantity,
            lineTotal = unitPrice.multiply(BigDecimal(ci.quantity)),
        )
    }
}
