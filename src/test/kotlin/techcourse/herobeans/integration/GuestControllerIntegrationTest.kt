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
import techcourse.herobeans.entity.Profile
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel
import techcourse.herobeans.repository.CoffeeJpaRepository

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

    @Test
    fun `should return all coffees`() {
        // TODO: extract to data.sql or DataBase.Fixtures
        (1..20).forEach { i ->
            coffeeJpaRepository.save(
                Coffee(
                    name = "Espresso$i",
                    profile =
                        Profile(
                            body = ProfileLevel.MEDIUM,
                            sweetness = ProfileLevel.LOW,
                            acidity = ProfileLevel.HIGH,
                        ),
                    taste = "Strong",
                    brewRecommendation = BrewRecommendation.ESPRESSO,
                    origin = OriginCountry.BRAZIL,
                    processingMethod = ProcessingMethod.WASHED_PROCESS,
                    roastLevel = RoastLevel.DARK_ROAST,
                    description = "Rich and bold",
                    imageUrl = "http://img/espresso.jpg",
                    id = 0L,
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
}
