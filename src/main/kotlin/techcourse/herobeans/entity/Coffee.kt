package techcourse.herobeans.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.Grams
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel
import java.time.LocalDateTime

// TODO: check CreationTimeStamp and UpdateTimestamp annotations?
// TODO: decide if we want to have a isVisible flag to denote if a product is currently visible in our webshop
@Entity
class Coffee(
    @Column(unique = true, nullable = false)
    val name: String,
    @Embedded
    val profile: Profile,
    @Column(nullable = false)
    val taste: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val brewRecommendation: BrewRecommendation,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val origin: OriginCountry,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val processingMethod: ProcessingMethod,
    @OneToMany(
        mappedBy = "coffee",
        cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    val options: MutableList<PackageOption> = mutableListOf(),
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val roastLevel: RoastLevel,
    @Column(nullable = false)
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    val description: String?,
    @Column(nullable = false)
    var imageUrl: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
) {
    val isAvailable: Boolean
        get() = options.sumOf { it.quantity } > 0

    fun addOption(option: PackageOption) {
        require(options.none { it.weight == option.weight }) { "option already has ${option.weight.value}" }

        options.add(option)
        // TODO: maybe updater stamp manually
    }

    fun removeOptionByWeight(weight: Grams) {
        require(options.size >= 2) { "Removing option would leave coffee without enough options" }
        require(options.any { it.weight == weight }) { "No option found for weight $weight" }

        options.removeIf { it.weight == weight }
        // TODO: if updater stamp manually apply, here should be updated
    }
}

@Embeddable
data class Profile(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val body: ProfileLevel,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val sweetness: ProfileLevel,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val acidity: ProfileLevel,
)
