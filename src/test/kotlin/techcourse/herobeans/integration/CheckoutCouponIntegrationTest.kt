package techcourse.herobeans.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import techcourse.herobeans.client.StripeClient
import techcourse.herobeans.dto.CheckoutStartRequest
import techcourse.herobeans.dto.CheckoutStartResponse
import techcourse.herobeans.dto.FinalizePaymentRequest
import techcourse.herobeans.dto.PaymentError
import techcourse.herobeans.dto.PaymentErrorCode
import techcourse.herobeans.dto.PaymentIntent
import techcourse.herobeans.dto.PaymentResult
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.entity.Address
import techcourse.herobeans.entity.Coffee
import techcourse.herobeans.entity.Coupon
import techcourse.herobeans.entity.DiscountType
import techcourse.herobeans.entity.Member
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.entity.Profile
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.Grams
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel
import techcourse.herobeans.mapper.AddressMapper.toDto
import techcourse.herobeans.repository.AddressJpaRepository
import techcourse.herobeans.repository.CartJpaRepository
import techcourse.herobeans.repository.CoffeeJpaRepository
import techcourse.herobeans.repository.CouponJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository
import techcourse.herobeans.repository.OrderJpaRepository
import techcourse.herobeans.repository.PackageOptionJpaRepository
import techcourse.herobeans.repository.PaymentJpaRepository
import techcourse.herobeans.service.CartService
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CheckoutCouponIntegrationTest {
    @Autowired
    private lateinit var cartService: CartService

    @Autowired
    private lateinit var memberRepository: MemberJpaRepository

    @Autowired
    private lateinit var coffeeRepository: CoffeeJpaRepository

    @Autowired
    private lateinit var packageOptionRepository: PackageOptionJpaRepository

    @Autowired
    private lateinit var cartRepository: CartJpaRepository

    @Autowired
    private lateinit var orderRepository: OrderJpaRepository

    @Autowired
    private lateinit var paymentRepository: PaymentJpaRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var couponRepository: CouponJpaRepository

    @MockitoBean
    private lateinit var stripeClient: StripeClient

    @Autowired
    private lateinit var addressRepository: AddressJpaRepository

    @LocalServerPort
    private var port: Int = 0
    val baseUrl get() = "http://localhost:$port"

    companion object {
        const val COUPON_VALID = "VALID"
        const val COUPON_EXPIRED = "INVALID_EXPIRES"
        const val COUPON_OTHER_USER = "INVALID_NOT_EXISTS_MEMBER"
    }

    private lateinit var coupons: Map<String, Coupon>
    private lateinit var token: String
    private lateinit var member: Member
    private lateinit var coffee: Coffee
    private lateinit var packageOptions: MutableList<PackageOption>
    private lateinit var address: Address

    @BeforeEach
    fun setUp() {
        // Clean up all repositories
        paymentRepository.deleteAll()
        orderRepository.deleteAll()
        cartRepository.deleteAll()
        addressRepository.deleteAll()
        memberRepository.deleteAll()
        packageOptionRepository.deleteAll()
        coffeeRepository.deleteAll()
        couponRepository.deleteAll()

        // Register user and get token
        val registrationRequest = RegistrationRequest("testuser", "test@test.com", "12345678")
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
        assertThat(token).isNotEmpty()

        // Init var member
        member = memberRepository.findByEmail("test@test.com")!!

        address =
            Address(
                street = "Oranienburger Str.",
                number = "70",
                city = "Berlin",
                postalCode = "10117",
                countryCode = "DE",
                member = member,
            )
        addressRepository.save(address)

        // Create test validate Coupon
        val testCouponsList =
            listOf(
                Coupon(
                    code = COUPON_VALID,
                    discountType = DiscountType.PERCENTAGE,
                    discountValue = BigDecimal(10),
                    expiresAt = LocalDateTime.now().plusDays(10),
                    userMail = "test@test.com",
                ),
                Coupon(
                    code = COUPON_EXPIRED,
                    discountType = DiscountType.PERCENTAGE,
                    discountValue = BigDecimal(10),
                    expiresAt = LocalDateTime.now().minusDays(1),
                    userMail = "test@test.com",
                ),
                Coupon(
                    code = COUPON_OTHER_USER,
                    discountType = DiscountType.PERCENTAGE,
                    discountValue = BigDecimal(10),
                    expiresAt = LocalDateTime.now().plusDays(10),
                    userMail = "i_am_not_exist@test.com",
                ),
            )

        // Add to Coupons
        coupons =
            testCouponsList.associateBy { it.code }
                .mapValues {
                    couponRepository.save(it.value)
                }

        // Create test coffee
        coffee =
            Coffee(
                name = "Test Coffee",
                profile =
                    Profile(
                        body = ProfileLevel.MEDIUM,
                        sweetness = ProfileLevel.HIGH,
                        acidity = ProfileLevel.LOW,
                    ),
                taste = "Rich and smooth",
                brewRecommendation = BrewRecommendation.ESPRESSO,
                origin = OriginCountry.BRAZIL,
                processingMethod = ProcessingMethod.WASHED_PROCESS,
                roastLevel = RoastLevel.MEDIUM_ROAST,
                description = "Perfect test coffee",
                imageUrl = "https://example.com/test.jpg",
            )
        coffee = coffeeRepository.save(coffee)

        // Create test package options
        packageOptions = mutableListOf()
        val option =
            PackageOption(
                quantity = 100,
                price = BigDecimal("25.50"),
                weight = Grams.G250,
                coffee = coffee,
            )
        packageOptions.add(packageOptionRepository.save(option))

        // Add first item to cart
        RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .post("/api/member/cart/${packageOptions[0].id}")
            .then()
            .statusCode(HttpStatus.CREATED.value())
    }

    @Test
    fun `should apply valid coupon discount during checkout`() {
        cartService.clearCart(member.id)

        val option1 = addPackageOption(Grams.G250, "10.00", 50)
        val option2 = addPackageOption(Grams.G500, "20.00", 50)

        addItemsToCart(1, 2) // total price in cart 3000
        val cart = cartRepository.findByMemberId(member.id)!!

        val paymentIntent =
            PaymentIntent(
                id = "pi_test_coupon",
                amount = 2700,
                status = "requires_payment_method",
                clientSecret = "pi_test_coupon_secret",
                currency = "eur",
            )
        whenever(stripeClient.createPaymentIntent(any(), any())).thenReturn(paymentIntent)

        val request =
            CheckoutStartRequest(
                paymentMethod = "pm_card_visa",
                couponCode = coupons[COUPON_VALID]?.code,
                addressId = address.id,
            )

        val response =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/checkout/start")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .`as`(CheckoutStartResponse::class.java)

        assertThat(response.amount).isEqualByComparingTo(BigDecimal("27.00"))

        val order = orderRepository.findByIdWithOrderItems(response.orderId).get()
    }

    @Test
    fun `should reject expired coupon`() {
        cartService.clearCart(member.id)
        addPackageOption(Grams.G250, "10.00", 50)
        addItemsToCart(1, 1)

        val request =
            CheckoutStartRequest(
                paymentMethod = "pm_card_visa",
                couponCode = COUPON_EXPIRED,
                addressId = 1L,
            )

        RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/checkout/start")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("message", CoreMatchers.containsString("expired"))
    }

    @Test
    fun `should reject coupon for different user`() {
        cartService.clearCart(member.id)
        addPackageOption(Grams.G250, "10.00", 50)
        addItemsToCart(1, 1)

        val request =
            CheckoutStartRequest(
                paymentMethod = "pm_card_visa",
                couponCode = COUPON_OTHER_USER,
                addressId = address.id,
            )

        RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/checkout/start")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("message", CoreMatchers.containsString("Invalid user email"))
    }

    fun addPackageOption(
        weight: Grams,
        price: String,
        quantity: Int = 50,
    ): PackageOption {
        val option =
            PackageOption(
                quantity = quantity,
                price = BigDecimal(price),
                weight = weight,
                coffee = coffee,
            )
        val savedOption = packageOptionRepository.save(option)
        packageOptions.add(savedOption)
        return savedOption
    }

    fun createPaymentIntentAndStartCheckout(
        paymentIntentId: String = "pi_test_${System.currentTimeMillis()}",
        amount: Int = 2550,
        status: String = "requires_payment_method",
    ): CheckoutStartResponse {
        val paymentIntent =
            PaymentIntent(
                id = paymentIntentId,
                amount = amount,
                status = status,
                clientSecret = "${paymentIntentId}_secret",
                currency = "eur",
            )
        whenever(stripeClient.createPaymentIntent(any(), any())).thenReturn(paymentIntent)

        val request = CheckoutStartRequest(paymentMethod = "pm_card_visa", addressId = 1L)
        return RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/checkout/start")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .`as`(CheckoutStartResponse::class.java)
    }

    fun finalizePayment(
        paymentIntentId: String,
        orderId: Long,
        paymentStatus: String = "succeeded",
    ): PaymentResult {
        val confirmedPaymentIntent =
            PaymentIntent(
                id = paymentIntentId,
                amount = 2550,
                status = paymentStatus,
                clientSecret = "${paymentIntentId}_secret",
                currency = "eur",
            )
        whenever(stripeClient.confirmPaymentIntent(paymentIntentId)).thenReturn(confirmedPaymentIntent)

        val finalizeRequest =
            FinalizePaymentRequest(
                paymentIntentId = paymentIntentId,
                orderId = orderId,
            )

        val finalizeResponse =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .body(finalizeRequest)
                .post("/api/checkout/finalize")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString()

        val jsonNode = objectMapper.readTree(finalizeResponse)
        return if (jsonNode.has("paymentStatus") && !jsonNode.has("error")) {
            PaymentResult.Success(
                orderId = jsonNode.get("orderId").asLong(),
                paymentStatus = jsonNode.get("paymentStatus").asText(),
                addressDto = address.toDto(),
            )
        } else {
            PaymentResult.Failure(
                orderId = jsonNode.get("orderId")?.asLong(),
                error = PaymentError(PaymentErrorCode.PAYMENT_FAILED, message = "Payment failed"),
            )
        }
    }

    fun verifyOrderStatus(
        orderId: Long,
        expectedStatus: String,
    ) {
        val order = orderRepository.findByIdWithOrderItems(orderId).get()
        assertThat(order.status.name).isEqualTo(expectedStatus)
    }

    fun verifyStockQuantity(
        optionIndex: Int,
        expectedQuantity: Int,
    ) {
        val option = packageOptionRepository.findById(packageOptions[optionIndex].id).get()
        assertThat(option.quantity).isEqualTo(expectedQuantity)
    }

    fun verifyCartIsEmpty() {
        val cartResponse =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .get("/api/member/cart")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .jsonPath()

        assertThat(cartResponse.getList<Any>("items")).isEmpty()
    }

    fun clearCart() {
        RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .delete("/api/member/cart/clear")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
    }

    /**
     * 1 quantity each
     */
    private fun addItemsToCart(
        from: Int,
        size: Int,
    ) {
        for (i in from until from + size) {
            addItemToCartByIndex(i)
        }
    }

    private fun addItemToCartByIndex(index: Int) {
        RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .post("/api/member/cart/${packageOptions[index].id}")
            .then()
            .statusCode(HttpStatus.CREATED.value())
    }

    fun BigDecimal.toCent() = this.multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toInt()
}
