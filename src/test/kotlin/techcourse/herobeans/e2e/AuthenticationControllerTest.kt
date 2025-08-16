package techcourse.herobeans.e2e

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import techcourse.herobeans.configuration.JwtTokenProvider
import techcourse.herobeans.dto.LoginRequest
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.repository.MemberJpaRepository

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthenticationControllerTest {
    @Autowired
    private lateinit var memberRepository: MemberJpaRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @LocalServerPort
    private var port: Int = 0

    val baseUrl get() = "http://localhost:$port"

    @BeforeEach
    fun setUp() {
        memberRepository.deleteAll()
    }

    @Test
    fun `should register new member and return valid token`() {
        val registrationRequest =
            RegistrationRequest(
                "test",
                "test@test.com",
                "12345678",
            )

        val token =
            RestAssured.given()
                .log().all()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(registrationRequest)
                .post("/api/members/register")
                .then()
                .statusCode(201)
                .extract().body().jsonPath().getString("token")

        assertThat(token).isNotEmpty()
        assertTrue(jwtTokenProvider.validateToken(token))

        val member = memberRepository.findByEmail("test@test.com")
        assertThat(member).isNotNull()
        assertThat(member?.name).isEqualTo("test")
        assertThat(member?.email).isEqualTo("test@test.com")
    }

    @Test
    fun `should return 409 when email already exists`() {
        val registrationRequest =
            RegistrationRequest(
                "test",
                "test@test.com",
                "12345678",
            )

        RestAssured.given()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .post("/api/members/register")
            .then().statusCode(201)

        RestAssured.given()
            .log().all()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .post("/api/members/register")
            .then().statusCode(409)
            .body("message", containsString(EMAIL_ALREADY_IN_USE))
    }

    @Test
    fun `should login with valid credentials and return valid token`() {
        val registrationRequest =
            RegistrationRequest(
                "test",
                "test@test.com",
                "12345678",
            )

        RestAssured.given()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .post("/api/members/register")
            .then()
            .statusCode(201)

        val loginRequest =
            LoginRequest(
                "test@test.com",
                "12345678",
            )

        val token =
            RestAssured.given()
                .log().all()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/members/login")
                .then()
                .statusCode(200)
                .extract()
                .body().jsonPath().getString("token")

        assertThat(token).isNotEmpty()
        assertTrue(jwtTokenProvider.validateToken(token))
    }

    @Test
    fun `should return 401 when login as non-registered member`() {
        val loginRequest =
            LoginRequest(
                "test@test.com",
                "12345678",
            )

        RestAssured.given()
            .log().all()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .post("/api/members/login")
            .then().statusCode(401)
    }

    @Test
    fun `should return 403 when login with invalid credentials`() {
        val registrationRequest =
            RegistrationRequest(
                "test",
                "test@test.com",
                "12345678",
            )

        RestAssured.given()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .post("/api/members/register")

        val loginRequest =
            LoginRequest(
                "test@test.com",
                "wrongpassword",
            )

        RestAssured.given()
            .log().all()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .post("/api/members/login")
            .then().statusCode(403)
    }

    @Test
    fun `should return 401 for request with invalid token`() {
        val token = "ndwndwoljdwpfkwkdsq.DNlwfk3wld'wamclwfjkepojfo3jf"
        RestAssured.given().log().all()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .`when`()
            .get("/api/members/me")
            .then().statusCode(401)
    }

    @Test
    fun `should return 401 without 'Authorization' header`() {
        val registrationRequest =
            RegistrationRequest(
                "test",
                "test@test.com",
                "12345678",
            )

        RestAssured.given()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .post("/api/members/register")
            .then()
            .statusCode(201)

        val loginRequest =
            LoginRequest(
                "test@test.com",
                "12345678",
            )

        val loginResponse =
            RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/members/login")
                .then().extract()

        val token = loginResponse.body().jsonPath().getString("token")
        RestAssured.given().log().all()
            .baseUri(baseUrl)
            .header("Location", "Bearer $token")
            .`when`()
            .get("/api/members/me")
            .then().statusCode(401)
    }

    @ParameterizedTest
    @MethodSource("invalidRegisterRequests")
    fun `should return 400 for not respecting email and password rules when registering`(registrationRequest: RegistrationRequest) {
        RestAssured.given()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .post("/api/members/register")
            .then().statusCode(400)
    }

    companion object {
        const val EMAIL_ALREADY_IN_USE = "Email already exists"

        @JvmStatic
        fun invalidRegisterRequests(): List<RegistrationRequest> =
            listOf(
                // invalid mail
                RegistrationRequest("test", "@", "abcdef1234"),
                // invalid mail
                RegistrationRequest("test", "a@", "abcdef1234"),
                // invalid mail
                RegistrationRequest("test", "@.com", "abcdef1234"),
                // invalid password: too short
                RegistrationRequest("test", "a@mail.com", "a".repeat(7)),
                // invalid password: too long
                RegistrationRequest("test", "a@mail.com", "a".repeat(65)),
                // invalid mail: empty
                RegistrationRequest("test", "", "abcdef1234"),
                // invalid password: empty
                RegistrationRequest("test", "a@mail.com", ""),
            )
    }
}
