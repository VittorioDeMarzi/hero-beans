package techcourse.herobeans.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import techcourse.herobeans.enums.ProfileLevel

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
