package techcourse.herobeans.e2e

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import techcourse.herobeans.dto.CoffeeDto
import techcourse.herobeans.dto.CoffeeRequest
import techcourse.herobeans.dto.LoginRequest
import techcourse.herobeans.dto.PackageOptionRequest
import techcourse.herobeans.dto.ProfileRequest
import techcourse.herobeans.entity.Member
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.Grams
import techcourse.herobeans.enums.MemberRole
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel
import techcourse.herobeans.repository.CoffeeJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository
import techcourse.herobeans.service.AuthenticationService
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminCaffeControllerE2ETest {
    @Autowired
    private lateinit var authenticationService: AuthenticationService

    @Autowired
    private lateinit var coffeeJpaRepository: CoffeeJpaRepository

    @Autowired
    private lateinit var memberJpaRepository: MemberJpaRepository

    @LocalServerPort
    private var port: Int = 0

    val baseUrl get() = "http://localhost:$port"

    private lateinit var token: String

    private fun validRequest() =
        CoffeeRequest(
            name = "Ethiopian Test",
            taste = "Floral, citrus, tea-like",
            brewRecommendation = BrewRecommendation.FILTER,
            origin = OriginCountry.ETHIOPIA,
            processingMethod = ProcessingMethod.HONEY_PROCESS,
            roastLevel = RoastLevel.LIGHT_ROAST,
            description = "Delicate and floral",
            imageUrl = "https://example.jpg",
            profile =
                ProfileRequest(
                    body = ProfileLevel.LOW,
                    sweetness = ProfileLevel.HIGH,
                    acidity = ProfileLevel.HIGH,
                ),
            options =
                listOf(
                    PackageOptionRequest(
                        quantity = 100,
                        weight = Grams.G250,
                        price = BigDecimal(22.0),
                    ),
                ),
        )

    private fun invalidRequest() =
        CoffeeRequest(
            name = "",
            taste = "",
            brewRecommendation = BrewRecommendation.FILTER,
            origin = OriginCountry.ETHIOPIA,
            processingMethod = ProcessingMethod.HONEY_PROCESS,
            roastLevel = RoastLevel.LIGHT_ROAST,
            description = null,
            imageUrl = "https://example.jpg",
            profile =
                ProfileRequest(
                    body = ProfileLevel.LOW,
                    sweetness = ProfileLevel.HIGH,
                    acidity = ProfileLevel.HIGH,
                ),
            options =
                listOf(
                    PackageOptionRequest(
                        quantity = 0,
                        weight = Grams.G250,
                        price = BigDecimal("0.00"),
                    ),
                ),
        )

    @BeforeEach
    fun setUp() {
        coffeeJpaRepository.deleteAll()
        memberJpaRepository.deleteAll()
        val admin =
            memberJpaRepository.save(
                Member(
                    name = "admin",
                    email = "admin@test.com",
                    password = "\$2a\$10\$y3rEiacoc/0F1Qh0mVweo.rYAAuyCbOGhuPI/fk3XnC20irt21.nm",
                    role = MemberRole.ADMIN,
                ),
            )

        token = authenticationService.login(LoginRequest(admin.email, "12345678")).token
    }

    @Test
    fun `createProduct returns 201 for ADMIN`() {
        val response =
            RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer $token")
                .body(validRequest())
                .post("/api/admin/coffees")
                .then()
                .statusCode(201)
                .extract()
                .`as`(CoffeeDto::class.java)

        assertThat(response.id).isNotNull
        assertThat(response.name).isEqualTo("Ethiopian Test")
        assertThat(response.taste).isEqualTo("Floral, citrus, tea-like")
        assertThat(response.brewRecommendation).isEqualTo(BrewRecommendation.FILTER)
        assertThat(response.origin).isEqualTo(OriginCountry.ETHIOPIA)
        assertThat(response.processingMethod).isEqualTo(ProcessingMethod.HONEY_PROCESS)
        assertThat(response.roastLevel).isEqualTo(RoastLevel.LIGHT_ROAST)
        assertThat(response.description).isEqualTo("Delicate and floral")
        assertThat(response.profile.body).isEqualTo(ProfileLevel.LOW)
        assertThat(response.profile.sweetness).isEqualTo(ProfileLevel.HIGH)
        assertThat(response.profile.acidity).isEqualTo(ProfileLevel.HIGH)
        assertThat(response.options).hasSize(1)
    }

    @Test
    fun `createProduct returns 403 for USER`() {
        val user =
            memberJpaRepository.save(
                Member(
                    name = "user",
                    email = "user@test.com",
                    password = "\$2a\$10\$y3rEiacoc/0F1Qh0mVweo.rYAAuyCbOGhuPI/fk3XnC20irt21.nm",
                    role = MemberRole.USER,
                ),
            )
        val userToken = authenticationService.login(LoginRequest(user.email, "12345678")).token

        RestAssured.given()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $userToken")
            .body(validRequest())
            .post("/api/admin/coffees")
            .then()
            .statusCode(403)
    }

    @Test
    fun `createProduct returns 400 for invalid body`() {
        RestAssured.given()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $token") // token admin dal setUp
            .body(invalidRequest())
            .post("/api/admin/coffees")
            .then()
            .statusCode(400)
    }

    @Test
    fun `deleteProduct returns 204 for ADMIN`() {
        // when
        val created =
            RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer $token")
                .body(validRequest())
                .post("/api/admin/coffees")
                .then()
                .statusCode(201)
                .extract()
                .`as`(CoffeeDto::class.java)

        val id = created.id

        // given
        RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .delete("/api/admin/coffees/{id}", id)
            .then()
            .statusCode(204)

        // then
        assertThat(coffeeJpaRepository.existsById(id)).isFalse()
    }
}
