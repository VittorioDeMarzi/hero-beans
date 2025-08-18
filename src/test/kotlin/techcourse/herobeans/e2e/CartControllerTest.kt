package techcourse.herobeans.e2e

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import techcourse.herobeans.dto.CartProductResponse
import techcourse.herobeans.dto.MessageResponseDto
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.enums.Grams
import techcourse.herobeans.repository.CartJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository
import techcourse.herobeans.repository.PackageOptionJpaRepository
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CartControllerTest {
    @Autowired lateinit var memberRepository: MemberJpaRepository

    @Autowired lateinit var cartRepository: CartJpaRepository

    @Autowired lateinit var optionRepository: PackageOptionJpaRepository

    @LocalServerPort private var port: Int = 0
    private val baseUrl get() = "http://localhost:$port"

    private lateinit var token: String

    @BeforeEach
    fun setUp() {
        cartRepository.deleteAll()
        optionRepository.deleteAll()
        memberRepository.deleteAll()

        val registrationRequest =
            RegistrationRequest(
                name = "test",
                email = "test@test.com",
                password = "12345678",
            )

        val response =
            RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(registrationRequest)
                .post("/api/members/register")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()

        token = response.body().jsonPath().getString("token")
        require(token.isNotBlank()) { "Registration did not return token" }
    }

    @Test
    fun `GET cart returns empty cart for new member`() {
        val cart =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .get("/api/member/cart")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .`as`(CartProductResponse::class.java)

        assertThat(cart.items).isEmpty()
        assertThat(cart.totalAmount).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `POST add product adds item to cart`() {
        val optionId = createOption(price = "9.90", quantity = 10)

        val created =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .post("/api/member/cart/$optionId")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .`as`(MessageResponseDto::class.java)

        assertThat(created.message).isEqualTo("Product added to cart")

        val cart =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .get("/api/member/cart")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .`as`(CartProductResponse::class.java)

        assertThat(cart.items).hasSize(1)
        val item = cart.items.first()
        assertThat(item.optionId).isEqualTo(optionId)
        assertThat(item.quantity).isEqualTo(1)
        assertThat(item.unitPrice).isEqualByComparingTo(BigDecimal("9.90"))
        assertThat(item.lineTotal).isEqualByComparingTo(BigDecimal("9.90"))
        assertThat(cart.totalAmount).isEqualByComparingTo(BigDecimal("9.90"))
    }

    @Test
    fun `POST same product twice increments quantity`() {
        val optionId = createOption(price = "5.00", quantity = 10)

        RestAssured.given().baseUri(baseUrl).header("Authorization", "Bearer $token")
            .post("/api/member/cart/$optionId").then().statusCode(HttpStatus.CREATED.value())

        RestAssured.given().baseUri(baseUrl).header("Authorization", "Bearer $token")
            .post("/api/member/cart/$optionId").then().statusCode(HttpStatus.CREATED.value())

        val cart =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .get("/api/member/cart")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .`as`(CartProductResponse::class.java)

        val item = cart.items.single()
        assertThat(item.quantity).isEqualTo(2)
        assertThat(item.lineTotal).isEqualByComparingTo(BigDecimal("10.00"))
        assertThat(cart.totalAmount).isEqualByComparingTo(BigDecimal("10.00"))
    }

    @Test
    fun `DELETE item removes it from cart`() {
        val optionId = createOption(price = "7.50", quantity = 5)

        RestAssured.given().baseUri(baseUrl).header("Authorization", "Bearer $token")
            .post("/api/member/cart/$optionId").then().statusCode(HttpStatus.CREATED.value())

        RestAssured.given().baseUri(baseUrl).header("Authorization", "Bearer $token")
            .delete("/api/member/cart/$optionId")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())

        val cart =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .get("/api/member/cart")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .`as`(CartProductResponse::class.java)

        assertThat(cart.items).isEmpty()
        assertThat(cart.totalAmount).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `DELETE clear empties the cart`() {
        val option1 = createOption(price = "3.00", quantity = 10)
        val option2 = createOption(price = "2.50", quantity = 10)

        RestAssured.given().baseUri(baseUrl).header("Authorization", "Bearer $token")
            .post("/api/member/cart/$option1").then().statusCode(HttpStatus.CREATED.value())
        RestAssured.given().baseUri(baseUrl).header("Authorization", "Bearer $token")
            .post("/api/member/cart/$option2").then().statusCode(HttpStatus.CREATED.value())

        RestAssured.given().baseUri(baseUrl).header("Authorization", "Bearer $token")
            .delete("/api/member/cart/clear")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())

        val cart =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .get("/api/member/cart")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .`as`(CartProductResponse::class.java)

        assertThat(cart.items).isEmpty()
        assertThat(cart.totalAmount).isEqualByComparingTo(BigDecimal.ZERO)
    }

    private fun createOption(
        price: String,
        quantity: Int,
    ): Long {
        val weight = Grams.entries.first()
        val opt =
            PackageOption(
                quantity = quantity,
                price = BigDecimal(price),
                weight = weight,
                coffee = null,
            )
        val saved = optionRepository.saveAndFlush(opt)
        return saved.id
    }
}
