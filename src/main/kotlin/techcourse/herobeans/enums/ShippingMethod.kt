package techcourse.herobeans.enums

import java.math.BigDecimal

// TODO: move somewhere related
val STANDARD_SHOPPING_FEE = BigDecimal("5.99")

enum class ShippingMethod(val shippingFee: BigDecimal) {
    STANDARD(STANDARD_SHOPPING_FEE),
    FREE(BigDecimal.ZERO),
    ;

    companion object {
        fun getShippingPolicy(amount: BigDecimal): ShippingMethod {
            return when {
                amount >= BigDecimal("50") -> FREE
                else -> STANDARD
            }
        }
    }
}
