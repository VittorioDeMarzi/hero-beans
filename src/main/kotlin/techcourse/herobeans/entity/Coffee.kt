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
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import techcourse.herobeans.enums.BrewRecommendation
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
    var name: String,
    @Embedded
    val profile: Profile,
    @Column(nullable = false)
    var taste: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var brewRecommendation: BrewRecommendation,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var origin: OriginCountry,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var processingMethod: ProcessingMethod,
    @OneToMany(
        mappedBy = "coffee",
        cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    val options: MutableList<PackageOption> = mutableListOf(),
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var roastLevel: RoastLevel,
    @Column(nullable = false)
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var description: String,
    @Column(nullable = false)
    var imageUrl: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    val isAvailable: Boolean
        get() = options.sumOf { it.quantity } > 0

    fun addOption(option: PackageOption) {
        require(options.none { it.weight == option.weight }) { "option already has ${option.weight.value}" }
        options.add(option)
        option.coffee = this
    }
}

@Embeddable
data class Profile(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var body: ProfileLevel,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var sweetness: ProfileLevel,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var acidity: ProfileLevel,
)
