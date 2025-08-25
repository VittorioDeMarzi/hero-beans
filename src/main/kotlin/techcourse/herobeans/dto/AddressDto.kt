package techcourse.herobeans.dto

class AddressDto(
    val street: String,
    val number: String,
    val city: String = "Berlin",
    val postalCode: String,
    val countryCode: String = "DE",
    val label: String? = null,
    val id: Long = 0L,
)
