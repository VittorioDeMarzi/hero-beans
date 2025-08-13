package techcourse.herobeans.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * id: Long
 * name: String
 * description: String
 * sweetness: SweetName
 * acidity: AcidityEnum
 * taste: String
 * origin: OriginEnum // TODO: should it be a country?
 * processingMethos: MethodEnum
 * options: List<SizeOption>
 * price: Bigdecimal?
 * isAvailable: Boolean
 * roastLevel: RoastEnum
 *
 * createdAt: LocalDateTime, val updatedAt: LocalDateTime
 */

@Entity
class Coffee private constructor(
    @Column(unique = true, nullable = false)
    val name: String,
//    val body: BodyLevel,
//    val sweetness: SweetnessLevel,
//    val acidity: AcidityLevel,
    @Column(nullable = false)
    val taste: String,
    @Column(nullable = false)
    val origin: OriginCountry,
    @Column(nullable = false)
    val processingMethod: ProcessingMethod,
    @OneToMany(
        mappedBy = "coffee",
        cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    val options: List<Option> = mutableListOf(),
    val pricePerKilo: BigDecimal,
    val isAvailable: Boolean,
    val roastLevel: RoastLevel,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val description: String?,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
) {
    constructor(name: String, description: String) : this() // null here
}
