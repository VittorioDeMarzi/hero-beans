package techcourse.herobeans.enums

import java.math.BigDecimal

enum class ShippingMethod {
    STANDARD,
    EXPRESS,
    FREE,
    ;

    fun feeForSubtotal(subtotal: BigDecimal): BigDecimal {
        return when (this) {
            FREE -> BigDecimal.ZERO
            STANDARD -> if (subtotal >= FREE_SHIPPING_THRESHOLD) BigDecimal.ZERO else STANDARD_FEE
            EXPRESS -> EXPRESS_FEE
        }
    }

    companion object {
        private val FREE_SHIPPING_THRESHOLD = BigDecimal("50.00")
        private val STANDARD_FEE = BigDecimal("3.90")
        private val EXPRESS_FEE = BigDecimal("6.90")
    }
}
