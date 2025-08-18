package techcourse.herobeans.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.Certificate
import techcourse.herobeans.enums.Grams
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel
import techcourse.herobeans.repository.CoffeeJpaRepository

@DataJpaTest
class CoffeeTest(
    @Autowired private val coffeeJpaRepository: CoffeeJpaRepository,
) {
    @Test
    fun `coffee should have all parameters after save`() {
        val profile =
            Profile(
                body = ProfileLevel.MEDIUM,
                sweetness = ProfileLevel.LOWEST,
                acidity = ProfileLevel.HIGH,
            )

        val option =
            PackageOption(
                weight = Grams.G250,
                price = 12.99.toBigDecimal(),
                quantity = 10,
                coffee = null,
            )

        val coffee =
            Coffee(
                name = "Ethiopian Yirgacheffe",
                profile = profile,
                taste = "Floral and fruity",
                brewRecommendation = BrewRecommendation.FRENCH_PRESS,
                origin = OriginCountry.ETHIOPIA,
                processingMethod = ProcessingMethod.WASHED_PROCESS,
                roastLevel = RoastLevel.LIGHT_ROAST,
                certificates =
                    mutableListOf(
                        Certificate.RAINFOREST_ALLIANCE,
                        Certificate.FAIRTRADE,
                        Certificate.EU_ORGANIC,
                        Certificate.BIOLAND,
                        Certificate.NATURLAND,
                        Certificate.DEMETER,
                    ),
                description = "Bright and aromatic coffee with citrus notes.",
                imageUrl = "https://example.com/images/ethiopian_yirgacheffe.jpg",
            )

        coffee.addOption(option)
        val savedCoffee = coffeeJpaRepository.save(coffee)
        assertThat(savedCoffee.id).isEqualTo(1L)
        assertThat(savedCoffee.profile.body).isEqualTo(ProfileLevel.MEDIUM)
        assertThat(savedCoffee.options[0].quantity).isEqualTo(10)
        assertThat(savedCoffee.options[0].price).isEqualTo(12.99.toBigDecimal())
        assertThat(savedCoffee.certificates).contains(Certificate.RAINFOREST_ALLIANCE, Certificate.FAIRTRADE)
    }
}
