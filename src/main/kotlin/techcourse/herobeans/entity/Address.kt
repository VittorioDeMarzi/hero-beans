package techcourse.herobeans.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

/**
 * label stands for "Home", "Work"
 */
@Entity
class Address(
    @Column(nullable = false)
    var street: String,
    @Column(nullable = false)
    var number: String,
    @Column(nullable = false)
    val city: String = "Berlin",
    @Column(nullable = false)
    var postalCode: String,
    @Column(nullable = false)
    val countryCode: String = "DE",
    var label: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    val member: Member,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    init {
        require(street.isNotEmpty()) { "Street cannot be empty" }
        require(number.isNotEmpty()) { "Number cannot be empty" }
        require(this.isInBerlin()) { "Out of shipping & billing zone" }
        require(this.isInGermany()) { "Out of shipping & billing zone" }
    }

    fun isInBerlin(): Boolean {
        return this.city.equals("Berlin", ignoreCase = true) &&
            (this.postalCode.length == 5 && isBerlinPostCode())
    }

    private fun isBerlinPostCode() = this.postalCode.startsWith("10") || this.postalCode.startsWith("12")

    fun isInGermany(): Boolean {
        return this.countryCode.equals("DE", ignoreCase = true)
    }
}
