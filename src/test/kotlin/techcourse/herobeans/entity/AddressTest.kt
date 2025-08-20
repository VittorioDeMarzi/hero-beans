package techcourse.herobeans.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
class AddressTest {
    private val member = Member(name = "Test", email = "test@test.com", password = "12345678")

    @Test
    fun `should create Address with valid data`() {
        val address =
            Address(
                street = "Alexanderplatz",
                number = "1",
                postalCode = "10178",
                member = member,
            )

        assertThat(address.street).isEqualTo("Alexanderplatz")
        assertThat(address.number).isEqualTo("1")
        assertThat(address.city).isEqualTo("Berlin") // default
        assertThat(address.countryCode).isEqualTo("DE") // default
        assertThat(address.member).isEqualTo(member)
        assertThat(address.isInBerlin()).isTrue
        assertThat(address.isInGermany()).isTrue
    }

    @Test
    fun `should throw exception if street is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            Address(
                street = "",
                number = "1",
                postalCode = "10178",
                member = member,
            )
        }
    }

    @Test
    fun `should throw exception if number is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            Address(
                street = "Alexanderplatz",
                number = "",
                postalCode = "10178",
                member = member,
            )
        }
    }

    @Test
    fun `should throw exception if city or postalCode is outside Berlin`() {
        assertThrows(IllegalArgumentException::class.java) {
            Address(
                street = "Some Street",
                number = "10",
                city = "Munich",
                postalCode = "80331",
                member = member,
            )
        }
    }

    @Test
    fun `should throw exception if country is not DE`() {
        assertThrows(IllegalArgumentException::class.java) {
            Address(
                street = "Alexanderplatz",
                number = "1",
                postalCode = "10178",
                member = member,
                countryCode = "US",
            )
        }
    }

    @Test
    fun `isInBerlin should return true for Berlin postal codes starting with 10 or 12`() {
        val addr1 = Address(street = "Street 1", number = "1", postalCode = "10115", member = member)
        val addr2 = Address(street = "Street 2", number = "2", postalCode = "12345", member = member)

        assertThat(addr1.isInBerlin()).isTrue
        assertThat(addr2.isInBerlin()).isTrue
    }

    @Test
    fun `isInGermany should return true for country code DE`() {
        val addr = Address(street = "Street 1", number = "1", postalCode = "10115", member = member)
        assertThat(addr.isInGermany()).isTrue
    }
}
