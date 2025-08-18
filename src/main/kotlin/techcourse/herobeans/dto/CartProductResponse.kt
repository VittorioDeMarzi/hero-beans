package techcourse.herobeans.dto

import java.math.BigDecimal

class CartProductResponse(
    val items: List<CartProductItem>,
    val totalAmount: BigDecimal,
)

class CartProductItem(
    val cartItemId: Long?,
    val optionId: Long,
    val productName: String,
    val optionName: String,
    val imageUrl: String?,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal,
)
