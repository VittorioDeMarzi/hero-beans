package techcourse.herobeans.e2e

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import techcourse.herobeans.configuration.JwtTokenProvider
import techcourse.herobeans.dto.LoginRequest
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.repository.MemberJpaRepository

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class AuthenticationControllerTest {
    lateinit var token: String

    @Autowired
    private lateinit var memberRepository: MemberJpaRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val registrationRequest =
        RegistrationRequest(
            "test",
            "test@test.com",
            "12345678",
        )

    @BeforeAll
    fun setUp() {
        val token =
            RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(registrationRequest)
                .post("/api/members/register")
                .then().extract().body().jsonPath().getString("token")
    }

    @Test
    fun registerMember() {
        assertThat(token).isNotEmpty
        assertTrue(jwtTokenProvider.validateToken(token))
        val member = memberRepository.findByEmail("test@test.com")
        assertThat(member?.name).isEqualTo("test")
    }

    @Test
    fun `should throw if email already used`() {
        RestAssured.given()
            .log().all()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .post("/api/members/register")
            .then()
            .statusCode(409)
            .body("message", containsString(EMAIL_ALREADY_IN_USE))
    }

    @Test
    fun `should log in and return a valid token`() {
        val loginRequest =
            LoginRequest(
                "test@test.com",
                "12345678",
            )

        val token =
            RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/members/login")
                .then().extract().body().jsonPath().getString("token")

        assertThat(token).isNotEmpty
        assertTrue(jwtTokenProvider.validateToken(token))
    }

    companion object {
        const val EMAIL_ALREADY_IN_USE = "Email already exists"
    }
}
