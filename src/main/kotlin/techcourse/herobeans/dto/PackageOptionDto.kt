package techcourse.herobeans.dto

import techcourse.herobeans.enums.Grams
import techcourse.herobeans.enums.StockStatus
import java.math.BigDecimal

class PackageOptionDto(
    val stockStatus: StockStatus,
    val quantity: Int,
    val price: BigDecimal,
    val weight: Grams,
    val id: Long,
)
