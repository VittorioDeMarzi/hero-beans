package techcourse.herobeans.e2e

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import techcourse.herobeans.configuration.JwtTokenProvider
import techcourse.herobeans.dto.LoginRequest
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.repository.MemberJpaRepository

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class AuthenticationControllerTest {
    @Autowired
    private lateinit var memberRepository: MemberJpaRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

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
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .post("/api/members/register")
            .then().statusCode(201)

        RestAssured.given()
            .log().all()
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
    fun `should return 403 when login with invalid credentials`() {
        val registrationRequest =
            RegistrationRequest(
                "test",
                "test@test.com",
                "12345678",
            )

        RestAssured.given()
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
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .post("/api/members/login")
            .then().statusCode(403)
    }

    companion object {
        const val EMAIL_ALREADY_IN_USE = "Email already exists"
    }
}
