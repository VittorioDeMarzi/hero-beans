package techcourse.herobeans.e2e

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import techcourse.herobeans.dto.AddressDto
import techcourse.herobeans.dto.AddressRequest
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.dto.UpdateAddressRequest
import techcourse.herobeans.repository.AddressJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository
import java.awt.List

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AddressControllerTest {
    @Autowired
    private lateinit var memberRepository: MemberJpaRepository

    @Autowired
    private lateinit var addressRepository: AddressJpaRepository

    @LocalServerPort
    private var port: Int = 0

    val baseUrl get() = "http://localhost:$port"

    private lateinit var token: String

    @BeforeEach
    fun setUp() {
        addressRepository.deleteAll()
        memberRepository.deleteAll()

        val registrationRequest = RegistrationRequest("test", "test@test.com", "12345678")
        val response =
            RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(registrationRequest)
                .post("/api/members/register")
                .then()
                .statusCode(201)
                .extract()

        token = response.body().jsonPath().getString("token")
    }

    @Test
    fun `should create new address`() {
        val request = AddressRequest("Street 1", "123", "Berlin", "12345", "DE", "Home")

        val addressDto =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/address")
                .then()
                .statusCode(201)
                .extract()
                .`as`(AddressDto::class.java)

        assertThat(addressDto.street).isEqualTo("Street 1")
        assertThat(addressDto.label).isEqualTo("Home")
    }

    @Test
    fun `should not create address if member has already 5 addresses`() {
        val requests =
            List(6) {
                AddressRequest("Street 1", "123", "Berlin", "12345", "DE", "Home")
            }
        var response: ExtractableResponse<Response>? = null
        requests.forEach {
            response =
                RestAssured.given()
                    .baseUri(baseUrl)
                    .header("Authorization", "Bearer $token")
                    .contentType(ContentType.JSON)
                    .body(it)
                    .post("/api/address")
                    .then()
                    .extract()
        }

        assertThat(response?.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should get all addresses`() {
        val request1 = AddressRequest("Street 1", "123", "Berlin", "12345", "DE", "Home")
        val request2 = AddressRequest("Street 2", "258", "Berlin", "12049", "DE", "Work")

        RestAssured.given().baseUri(
            baseUrl,
        ).header("Authorization", "Bearer $token").contentType(ContentType.JSON).body(request1).post("/api/address")
        RestAssured.given().baseUri(
            baseUrl,
        ).header("Authorization", "Bearer $token").contentType(ContentType.JSON).body(request2).post("/api/address")

        val addresses =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .get("/api/address")
                .then()
                .statusCode(200)
                .extract()
                .`as`(Array<AddressDto>::class.java)

        assertThat(addresses.size).isEqualTo(2)
    }

    @Test
    fun `should update address`() {
        val createRequest = AddressRequest("Street 1", "123", "Berlin", "12345", "DE", "Home")

        val addressDto =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .body(createRequest)
                .post("/api/address")
                .then()
                .statusCode(201)
                .extract()
                .`as`(AddressDto::class.java)

        val updateRequest = UpdateAddressRequest(street = "Updated Street")

        val updatedDto =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .patch("/api/address/${addressDto.id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(AddressDto::class.java)

        assertThat(updatedDto.street).isEqualTo("Updated Street")
    }

    @Test
    fun `should delete address`() {
        val createRequest = AddressRequest("Street 1", "123", "Berlin", "12345", "DE", "Home")
        val addressDto =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .body(createRequest)
                .post("/api/address")
                .then()
                .statusCode(201)
                .extract()
                .`as`(AddressDto::class.java)

        RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .delete("/api/address/${addressDto.id}")
            .then()
            .statusCode(204)
    }
}
