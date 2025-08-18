package techcourse.herobeans.dto

import techcourse.herobeans.enums.Grams
import java.math.BigDecimal

class PackageOptionDto(
    val stockStatus: String,
    val quantity: Int,
    val price: BigDecimal,
    val weight: Grams,
    val id: Long,
)
