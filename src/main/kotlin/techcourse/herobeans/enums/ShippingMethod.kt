package techcourse.herobeans.enums

import java.math.BigDecimal

enum class ShippingMethod(val shippingFee: BigDecimal) {
    STANDARD(BigDecimal("5.99")),
    FREE(BigDecimal.ZERO),
    ;

    companion object {
        fun getShippingPolicy(amount: BigDecimal): ShippingMethod {
            return when {
                amount >= BigDecimal("50") -> ShippingMethod.FREE
                else -> ShippingMethod.STANDARD
            }
        }
    }
}
