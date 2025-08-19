package techcourse.herobeans.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import techcourse.herobeans.entity.Coffee
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.entity.Profile
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.Grams
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel
import techcourse.herobeans.repository.CoffeeJpaRepository
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc // mockMvc is lighter than SpringBootTest
@ActiveProfiles("test")
class GuestControllerIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var coffeeJpaRepository: CoffeeJpaRepository

    @BeforeEach
    fun setup() {
        coffeeJpaRepository.deleteAll()
    }

    // Helpers
    private fun createCoffee(
        name: String,
        origin: OriginCountry,
        roast: RoastLevel,
        brew: BrewRecommendation,
        weightOption: PackageOption = PackageOption(10, BigDecimal(10), Grams.G250),
        processing: ProcessingMethod = ProcessingMethod.WASHED_PROCESS,
    ): Coffee {
        return Coffee(
            name = name,
            profile =
                Profile(
                    body = ProfileLevel.MEDIUM,
                    sweetness = ProfileLevel.LOW,
                    acidity = ProfileLevel.HIGH,
                ),
            taste = "Strong",
            brewRecommendation = brew,
            origin = origin,
            processingMethod = processing,
            roastLevel = roast,
            description = "Rich and bold",
            imageUrl = "http://img/espresso.jpg",
            options = mutableListOf(weightOption),
        )
    }

    private fun saveAll(vararg coffees: Coffee) {
        coffeeJpaRepository.saveAll(coffees.toList())
    }

    @Test
    fun `should return 8 coffe with pagination coffees`() {
        (1..20).forEach { i ->
            coffeeJpaRepository.save(
                createCoffee(
                    name = "Espresso$i",
                    origin = OriginCountry.BRAZIL,
                    roast = RoastLevel.DARK_ROAST,
                    brew = BrewRecommendation.ESPRESSO,
                ),
            )
        }
        mockMvc.get("/api/product")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.content") { isArray() }
                jsonPath("$.content[0].name") { value("Espresso1") }
                jsonPath("$.content[1].name") { value("Espresso2") }
                jsonPath("$.content[7].name") { value("Espresso8") }
                jsonPath("$.totalElements") { value(20) }
                jsonPath("$.size") { value(8) }
            }
    }

    @Test
    fun `should return coffee by id`() {
        val cappuccino =
            coffeeJpaRepository.save(
                Coffee(
                    name = "Cappuccino",
                    profile =
                        Profile(
                            body = ProfileLevel.LOW,
                            sweetness = ProfileLevel.HIGH,
                            acidity = ProfileLevel.MEDIUM,
                        ),
                    taste = "Balanced",
                    brewRecommendation = BrewRecommendation.FILTER,
                    origin = OriginCountry.COLOMBIA,
                    processingMethod = ProcessingMethod.NATURAL_PROCESS,
                    roastLevel = RoastLevel.MEDIUM_ROAST,
                    description = "Smooth and creamy",
                    imageUrl = "http://img/cappuccino.jpg",
                    id = 0L,
                ),
            )

        mockMvc.get("/api/product/${cappuccino.id}")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.id") { value(cappuccino.id!!) }
                jsonPath("$.name") { value("Cappuccino") }
            }
    }

    @Test
    fun `should return 404 when coffee id does not exist`() {
        mockMvc.get("/api/product/999999")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should sort by name ASC`() {
        saveAll(
            createCoffee("Zeta", OriginCountry.BRAZIL, RoastLevel.DARK_ROAST, BrewRecommendation.ESPRESSO),
            createCoffee("Alpha", OriginCountry.BRAZIL, RoastLevel.DARK_ROAST, BrewRecommendation.ESPRESSO),
            createCoffee("Mango", OriginCountry.BRAZIL, RoastLevel.DARK_ROAST, BrewRecommendation.ESPRESSO),
        )

        mockMvc.get("/api/product") {
            param("sort", "NAME_ASC")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].name") { value("Alpha") }
            jsonPath("$.content[1].name") { value("Mango") }
            jsonPath("$.content[2].name") { value("Zeta") }
        }
    }

    @Test
    fun `should sort by name DESC`() {
        saveAll(
            createCoffee("Zeta", OriginCountry.BRAZIL, RoastLevel.DARK_ROAST, BrewRecommendation.ESPRESSO),
            createCoffee("Alpha", OriginCountry.BRAZIL, RoastLevel.DARK_ROAST, BrewRecommendation.ESPRESSO),
            createCoffee("Mango", OriginCountry.BRAZIL, RoastLevel.DARK_ROAST, BrewRecommendation.ESPRESSO),
        )

        mockMvc.get("/api/product") {
            param("sort", "NAME_DESC") // Adatta al tuo enum
            param("size", "10")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].name") { value("Zeta") }
            jsonPath("$.content[1].name") { value("Mango") }
            jsonPath("$.content[2].name") { value("Alpha") }
        }
    }

    @Test
    fun `should filter by origin`() {
        saveAll(
            createCoffee("Zeta", OriginCountry.COLOMBIA, RoastLevel.MEDIUM_ROAST, BrewRecommendation.FILTER),
            createCoffee("Alpha", OriginCountry.BRAZIL, RoastLevel.DARK_ROAST, BrewRecommendation.ESPRESSO),
            createCoffee("Mango", OriginCountry.ETHIOPIA, RoastLevel.LIGHT_ROAST, BrewRecommendation.FRENCH_PRESS),
        )

        mockMvc.get("/api/product") {
            param("origin", "ETHIOPIA")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].name") { value("Mango") }
            jsonPath("$.totalElements") { value(1) }
        }
    }

    @Test
    fun `should filter by roast level`() {
        saveAll(
            createCoffee("Zeta", OriginCountry.COLOMBIA, RoastLevel.MEDIUM_ROAST, BrewRecommendation.FILTER),
            createCoffee("Alpha", OriginCountry.BRAZIL, RoastLevel.DARK_ROAST, BrewRecommendation.ESPRESSO),
            createCoffee("Mango", OriginCountry.ETHIOPIA, RoastLevel.LIGHT_ROAST, BrewRecommendation.FRENCH_PRESS),
        )

        mockMvc.get("/api/product") {
            param("roastLevel", "LIGHT_ROAST")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].name") { value("Mango") }
            jsonPath("$.totalElements") { value(1) }
        }
    }

    @Test
    fun `should filter by brewRecommendation`() {
        saveAll(
            createCoffee("Zeta", OriginCountry.COLOMBIA, RoastLevel.MEDIUM_ROAST, BrewRecommendation.FILTER),
            createCoffee("Alpha", OriginCountry.BRAZIL, RoastLevel.DARK_ROAST, BrewRecommendation.ESPRESSO),
            createCoffee("Mango", OriginCountry.ETHIOPIA, RoastLevel.LIGHT_ROAST, BrewRecommendation.FRENCH_PRESS),
        )

        mockMvc.get("/api/product") {
            param("brewRecommendation", "ESPRESSO")
            param("brewRecommendation", "FILTER")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(2) }
        }
    }

    @Test
    fun `should filter by name like`() {
        saveAll(
            createCoffee("Zeta", OriginCountry.COLOMBIA, RoastLevel.MEDIUM_ROAST, BrewRecommendation.FILTER),
            createCoffee("Alpha", OriginCountry.BRAZIL, RoastLevel.DARK_ROAST, BrewRecommendation.ESPRESSO),
            createCoffee("Mango", OriginCountry.ETHIOPIA, RoastLevel.LIGHT_ROAST, BrewRecommendation.FRENCH_PRESS),
        )

        mockMvc.get("/api/product") {
            param("name", "ng")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(1) }
            jsonPath("$.content[0].name") { value("Mango") }
        }
    }
}
