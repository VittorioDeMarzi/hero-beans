package techcourse.herobeans.e2e

import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import techcourse.herobeans.dto.CouponResponse
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.entity.Coupon
import techcourse.herobeans.entity.DiscountType
import techcourse.herobeans.repository.CouponJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository
import techcourse.herobeans.service.AuthenticationService
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CouponControllerTest() {
    @Autowired
    private lateinit var memberJpaRepository: MemberJpaRepository

    @Autowired
    private lateinit var couponJpaRepository: CouponJpaRepository

    @Autowired
    private lateinit var authenticationService: AuthenticationService

    @LocalServerPort
    private var port: Int = 0

    val baseUrl get() = "http://localhost:$port"

    private lateinit var token: String
    private var userMail = "test@email.com"

    @BeforeEach
    fun setUp() {
        couponJpaRepository.deleteAll()

        token = authenticationService.register(RegistrationRequest("user", userMail, "12345678")).token
    }

    @Test
    fun `correct amount of coupons (welcome + generic) is listed`() {
        val welcomeCouponCount = 1
        val amountOfCoupons = 5
        val coupons =
            List(amountOfCoupons) { i ->
                Coupon(
                    code = "TEST-COUPON$i",
                    discountType = DiscountType.PERCENTAGE,
                    discountValue = BigDecimal(15),
                    expiresAt = LocalDateTime.now().plusDays(i.toLong()),
                    userMail = userMail,
                )
            }
        couponJpaRepository.saveAll(coupons)
        val response =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .get("api/me/coupons")
                .then()
                .statusCode(200)
                .extract()
                .`as`(Array<CouponResponse>::class.java)

        assertEquals(amountOfCoupons + welcomeCouponCount, response.size)
    }
}
