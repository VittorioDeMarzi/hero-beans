package techcourse.herobeans.e2e

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
import techcourse.herobeans.entity.Member
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.entity.Profile
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.Grams
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.PaymentStatus
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel
import techcourse.herobeans.repository.AddressJpaRepository
import techcourse.herobeans.repository.CartJpaRepository
import techcourse.herobeans.repository.CoffeeJpaRepository
import techcourse.herobeans.repository.CouponJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository
import techcourse.herobeans.repository.OrderJpaRepository
import techcourse.herobeans.repository.PackageOptionJpaRepository
import techcourse.herobeans.repository.PaymentJpaRepository
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CheckoutControllerTest {
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

    @Autowired
    private lateinit var addressRepository: AddressJpaRepository

    @MockitoBean
    private lateinit var stripeClient: StripeClient

    @LocalServerPort
    private var port: Int = 0
    val baseUrl get() = "http://localhost:$port"

    private lateinit var token: String
    private lateinit var coffee: Coffee
    private lateinit var packageOptions: MutableList<PackageOption>
    private lateinit var member: Member
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

        val memberEmail = "test@test.com"
        // Register user and get token
        val registrationRequest = RegistrationRequest("testuser", memberEmail, "12345678")
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

        member = memberRepository.findByEmail(memberEmail)!!

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
    fun `should start checkout successfully`() {
        val paymentIntent =
            PaymentIntent(
                id = "pi_test_12345",
                amount = 2550,
                status = "requires_payment_method",
                clientSecret = "pi_test_12345_secret_abc123",
                currency = "eur",
            )
        whenever(stripeClient.createPaymentIntent(any(), any())).thenReturn(paymentIntent)
        val request = CheckoutStartRequest(paymentMethod = "pm_card_visa", addressId = address.id)
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

        assertThat(response.paymentIntentId).isEqualTo("pi_test_12345")
        assertThat(response.amount).isEqualByComparingTo(BigDecimal("25.50"))
        assertThat(response.status).isEqualTo(PaymentStatus.PENDING)
        assertThat(response.clientSecret).isEqualTo("pi_test_12345_secret_abc123")
        assertThat(response.orderId).isNotNull()

        val order = orderRepository.findByIdWithOrderItems(response.orderId).get()
        assertThat(order.coffeeSubTotal).isEqualByComparingTo(BigDecimal("25.50"))
        assertThat(order.orderItems).hasSize(1)

        val updatedOption = packageOptionRepository.findById(packageOptions[0].id).get()
        assertThat(updatedOption.quantity).isEqualTo(99)
    }

    @Test
    fun `should finalize checkout successfully with succeeded payment`() {
        val startPaymentIntent =
            PaymentIntent(
                id = "pi_test_67890",
                amount = 2550,
                status = "requires_payment_method",
                clientSecret = "pi_test_67890_secret_def456",
                currency = "eur",
            )
        whenever(stripeClient.createPaymentIntent(any(), any())).thenReturn(startPaymentIntent)

        val startRequest = CheckoutStartRequest(paymentMethod = "pm_card_visa", addressId = address.id)
        val startResponse =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .body(startRequest)
                .post("/api/checkout/start")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .`as`(CheckoutStartResponse::class.java)

        val confirmedPaymentIntent =
            PaymentIntent(
                id = "pi_test_67890",
                amount = 2550,
                status = "succeeded",
                clientSecret = "pi_test_67890_secret_def456",
                currency = "eur",
            )
        whenever(stripeClient.confirmPaymentIntent("pi_test_67890")).thenReturn(confirmedPaymentIntent)

        val finalizeRequest =
            FinalizePaymentRequest(
                paymentIntentId = "pi_test_67890",
                orderId = startResponse.orderId,
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
        val result =
            if (jsonNode.has("paymentStatus") && !jsonNode.has("error")) {
                PaymentResult.Success(
                    orderId = jsonNode.get("orderId").asLong(),
                    paymentStatus = jsonNode.get("paymentStatus").asText(),
                )
            } else {
                PaymentResult.Failure(
                    orderId = jsonNode.get("orderId")?.asLong(),
                    error = PaymentError(PaymentErrorCode.PAYMENT_FAILED, message = "Payment failed"),
                )
            }

        assertThat(result).isInstanceOf(PaymentResult.Success::class.java)
        val successResult = result as PaymentResult.Success
        assertThat(successResult.orderId).isEqualTo(startResponse.orderId)
        assertThat(successResult.paymentStatus).isEqualTo("COMPLETED")

        val order = orderRepository.findByIdWithOrderItems(startResponse.orderId).get()
        assertThat(order.status.name).isEqualTo("PAID")

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

    @Test
    fun `should handle payment failure and rollback stock`() {
        val startResponse = createPaymentIntentAndStartCheckout("pi_test_failed")
        val result = finalizePayment("pi_test_failed", startResponse.orderId, "requires_payment_method")

        assertThat(result).isInstanceOf(PaymentResult.Failure::class.java)
        val failureResult = result as PaymentResult.Failure
        assertThat(failureResult.orderId).isEqualTo(startResponse.orderId)

        verifyStockQuantity(0, 100)
        verifyOrderStatus(startResponse.orderId, "PAYMENT_FAILED")
    }

    @Test
    fun `should return 400 when trying to checkout with empty cart`() {
        clearCart()

        val request = CheckoutStartRequest(paymentMethod = "pm_card_visa", addressId = address.id)

        RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/checkout/start")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("message", CoreMatchers.containsString("Cart is empty"))
    }

    @Test
    fun `should return 404 when trying to finalize with invalid order`() {
        val request =
            FinalizePaymentRequest(
                paymentIntentId = "pi_invalid",
                orderId = 99999L,
            )

        RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/checkout/finalize")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun `should handle insufficient stock during checkout`() {
        val option = packageOptionRepository.findById(packageOptions[0].id).get()
        option.quantity = 0
        packageOptionRepository.save(option)

        RestAssured.given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $token")
            .post("/api/member/cart/${packageOptions[0].id}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("message", CoreMatchers.containsString("Insufficient quantity"))
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

        val request = CheckoutStartRequest(paymentMethod = "pm_card_visa", addressId = address.id)
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

    @Test
    fun `should handle multiple items in cart and complete full payment flow`() {
        addPackageOption(Grams.G500, "45.00", 30)
        addPackageOption(Grams.G1000, "80.00", 20)

        addItemsToCart(1, 3)

        val totalCents = 15050

        val startResponse =
            createPaymentIntentAndStartCheckout(
                paymentIntentId = "pi_test_multi_items",
                amount = totalCents,
            )

        assertThat(startResponse.amount).isEqualByComparingTo(BigDecimal("150.50"))

        val order = orderRepository.findByIdWithOrderItems(startResponse.orderId).get()
        assertThat(order.orderItems).hasSize(3)
        assertThat(order.coffeeSubTotal).isEqualByComparingTo(BigDecimal("150.50"))

        val result = finalizePayment("pi_test_multi_items", startResponse.orderId, "succeeded")

        assertThat(result).isInstanceOf(PaymentResult.Success::class.java)
        verifyOrderStatus(startResponse.orderId, "PAID")
        verifyCartIsEmpty()

        verifyStockQuantity(0, 99)
        verifyStockQuantity(1, 29)
        verifyStockQuantity(2, 19)
    }

    @Test
    fun `should handle edge case - payment succeeded after initial failure`() {
        val startResponse = createPaymentIntentAndStartCheckout("pi_test_delayed_success")

        val initialResult = finalizePayment("pi_test_delayed_success", startResponse.orderId, "requires_payment_method")

        assertThat(initialResult).isInstanceOf(PaymentResult.Failure::class.java)
        verifyOrderStatus(startResponse.orderId, "PAYMENT_FAILED")
        verifyStockQuantity(0, 100)

        val delayedSuccessIntent =
            PaymentIntent(
                id = "pi_test_delayed_success",
                amount = 2550,
                status = "succeeded",
                clientSecret = "pi_test_delayed_success_secret",
                currency = "eur",
            )
        whenever(stripeClient.confirmPaymentIntent("pi_test_delayed_success")).thenReturn(delayedSuccessIntent)

        val webhookResponse =
            RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .body(FinalizePaymentRequest("pi_test_delayed_success", startResponse.orderId))
                .post("/api/checkout/finalize")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString()

        val jsonNode = objectMapper.readTree(webhookResponse)
        val result =
            if (jsonNode.has("paymentStatus") && !jsonNode.has("error")) {
                PaymentResult.Success(
                    orderId = jsonNode.get("orderId").asLong(),
                    paymentStatus = jsonNode.get("paymentStatus").asText(),
                )
            } else {
                PaymentResult.Failure(
                    orderId = jsonNode.get("orderId")?.asLong(),
                    error = PaymentError(PaymentErrorCode.PAYMENT_FAILED, message = "Payment failed"),
                )
            }
    }

    private fun addItemsToCart(
        from: Int,
        to: Int,
    ) {
        for (i in from until to) {
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
}
