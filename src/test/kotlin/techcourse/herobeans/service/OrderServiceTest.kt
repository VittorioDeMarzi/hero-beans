package techcourse.herobeans.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import techcourse.herobeans.entity.Cart
import techcourse.herobeans.entity.CartItem
import techcourse.herobeans.entity.Coffee
import techcourse.herobeans.entity.Member
import techcourse.herobeans.entity.Order
import techcourse.herobeans.entity.OrderItem
import techcourse.herobeans.entity.PackageOption
import techcourse.herobeans.entity.Profile
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.Grams
import techcourse.herobeans.enums.MemberRole
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.ProfileLevel
import techcourse.herobeans.enums.RoastLevel
import techcourse.herobeans.enums.ShippingMethod
import techcourse.herobeans.repository.OrderJpaRepository
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
class OrderServiceTest {
    @Mock
    private lateinit var orderRepository: OrderJpaRepository

    @Mock
    private lateinit var optionService: PackageOptionService

    @InjectMocks
    private lateinit var orderService: OrderService

    private lateinit var member: Member
    private lateinit var coffee: Coffee

    /**
     * class for Test Order Service
     */
    class TestOrderItem(
        val optionId: Long,
        val initialStock: Int,
        val orderQuantity: Int,
        val price: BigDecimal,
        val weight: Grams,
    ) {
        val expectedStockAfterOrder: Int = initialStock - orderQuantity
    }

    fun TestOrderItem.toPackageOption() =
        PackageOption(
            quantity = this.initialStock,
            price = this.price,
            weight = this.weight,
            coffee = coffee,
            id = this.optionId,
        )

    fun List<TestOrderItem>.toPackageOptions() =
        this.map { item ->
            item.toPackageOption()
        }

    fun TestOrderItem.toOrderItem() =
        OrderItem(
            optionId = this.optionId,
            productName = coffee.name,
            optionName = this.weight.name,
            quantity = this.orderQuantity,
            price = this.price,
        )

    fun List<TestOrderItem>.toOrderItems() =
        this.map { item ->
            item.toOrderItem()
        }.toMutableList()

    @BeforeEach
    fun setUp() {
        member =
            Member(
                name = "Test User",
                email = "test@example.com",
                password = "password",
                role = MemberRole.USER,
                id = 1L,
            )

        coffee =
            Coffee(
                name = "Test Coffee",
                profile = Profile(ProfileLevel.MEDIUM, ProfileLevel.HIGH, ProfileLevel.LOW),
                taste = "Smooth",
                brewRecommendation = BrewRecommendation.FILTER,
                origin = OriginCountry.BRAZIL,
                processingMethod = ProcessingMethod.WASHED_PROCESS,
                roastLevel = RoastLevel.MEDIUM_ROAST,
                description = "Test coffee",
                imageUrl = "https://example.com/image.jpg",
                id = 1L,
            )
    }

    @Test
    fun `should process order and reduce stock correctly`() {
        val testItems =
            listOf(
                TestOrderItem(
                    optionId = 1L,
                    initialStock = 10,
                    orderQuantity = 2,
                    price = BigDecimal("25.00"),
                    weight = Grams.G250,
                ),
                TestOrderItem(
                    optionId = 2L,
                    initialStock = 5,
                    orderQuantity = 1,
                    price = BigDecimal("45.00"),
                    weight = Grams.G500,
                ),
            )

        val options = testItems.toPackageOptions()

        assertThat(options[0].quantity).isEqualTo(10)
        assertThat(options[1].quantity).isEqualTo(5)

        val cart =
            Cart(member).apply {
                testItems.forEachIndexed { index, item ->
                    addOrIncrement(CartItem(this, options[index], item.orderQuantity))
                }
            }

        whenever(optionService.findByIdsWithLock(testItems.map { it.optionId }))
            .thenReturn(options)
        whenever(orderRepository.save(any<Order>()))
            .thenAnswer { it.getArgument(0) }
        whenever(optionService.saveAll(any<List<PackageOption>>()))
            .thenReturn(options)

        val result = orderService.processOrderWithStockReduction(cart)

        testItems.forEachIndexed { index, item ->
            assertThat(options[index].quantity)
                .withFailMessage("Option ${item.optionId} stock should be ${item.expectedStockAfterOrder}")
                .isEqualTo(item.expectedStockAfterOrder)
        }

        verify(orderRepository, times(1)).save(any<Order>())
        verify(optionService, times(1)).saveAll(any<List<PackageOption>>())

        assertThat(result.memberId).isEqualTo(member.id)
        assertThat(result.orderItems).hasSize(testItems.size)
    }

    @Test
    fun `should rollback stock correctly`() {
        val testItems =
            listOf(
                TestOrderItem(1L, initialStock = 8, orderQuantity = 2, BigDecimal("25.00"), Grams.G250),
                TestOrderItem(2L, initialStock = 4, orderQuantity = 1, BigDecimal("45.00"), Grams.G500),
            )

        val options = testItems.toPackageOptions()

        val order =
            Order(
                memberId = member.id,
                orderItems = testItems.toOrderItems(),
                shippingFee = BigDecimal.ZERO,
                shippingMethod = ShippingMethod.FREE,
            )

        whenever(optionService.findByIdsWithLock(testItems.map { it.optionId }))
            .thenReturn(options)
        whenever(optionService.saveAll(any<List<PackageOption>>())).thenReturn(options)
        whenever(orderRepository.save(any<Order>())).thenReturn(order)

        orderService.rollbackOptionsStock(order)

        testItems.forEachIndexed { index, item ->
            val expectedStock = item.orderQuantity + item.initialStock
            assertThat(options[index].quantity)
                .withFailMessage(
                    "Option ${options[index].id}'s stock, actual: ${options[index].quantity}, " +
                        "expect: $expectedStock",
                )
                .isEqualTo(expectedStock)
        }

        verify(optionService, times(1)).saveAll(any<List<PackageOption>>())
        verify(orderRepository, times(1)).save(any<Order>())
    }

    @Test
    fun `should apply shipping policy correctly`() {
        val testItem =
            TestOrderItem(
                optionId = 1L,
                initialStock = 10,
                orderQuantity = 1,
                price = STANDARD_SHIPPING_AMOUNT,
                weight = Grams.G250,
            )

        val option = testItem.toPackageOption()

        val cart =
            Cart(member).apply {
                addOrIncrement(CartItem(this, option, testItem.orderQuantity))
            }

        whenever(optionService.findByIdsWithLock(listOf(testItem.optionId)))
            .thenReturn(listOf(option))
        whenever(orderRepository.save(any<Order>()))
            .thenAnswer { it.getArgument(0) }
        whenever(optionService.saveAll(any<List<PackageOption>>()))
            .thenReturn(listOf(option))

        orderService.processOrderWithStockReduction(cart)

        val orderCaptor = ArgumentCaptor.forClass(Order::class.java)
        verify(orderRepository).save(orderCaptor.capture())

        val capturedOrder = orderCaptor.value
        assertThat(capturedOrder.shippingMethod).isEqualTo(ShippingMethod.STANDARD)
        assertThat(capturedOrder.shippingFee).isEqualTo(STANDARD_SHIPPING_FEE)
        assertThat(capturedOrder.coffeeSubTotal).isEqualTo(STANDARD_SHIPPING_AMOUNT)
    }

    @Test
    fun `should handle single item order correctly`() {
        val testItem =
            TestOrderItem(
                optionId = 1L,
                initialStock = 10,
                orderQuantity = 3,
                price = STANDARD_SHIPPING_AMOUNT,
                weight = Grams.G250,
            )

        val option = testItem.toPackageOption()

        val cart =
            Cart(member).apply {
                addOrIncrement(CartItem(this, option, testItem.orderQuantity))
            }

        whenever(optionService.findByIdsWithLock(listOf(testItem.optionId)))
            .thenReturn(listOf(option))
        whenever(orderRepository.save(any<Order>()))
            .thenAnswer { it.getArgument(0) }
        whenever(optionService.saveAll(any<List<PackageOption>>()))
            .thenReturn(listOf(option))

        val result = orderService.processOrderWithStockReduction(cart)

        assertThat(option.quantity).isEqualTo(testItem.expectedStockAfterOrder)

        val orderCaptor = ArgumentCaptor.forClass(Order::class.java)
        verify(orderRepository).save(orderCaptor.capture())

        val capturedOrder = orderCaptor.value

        assertThat(capturedOrder.orderItems).hasSize(1)
        assertThat(capturedOrder.orderItems[0].quantity).isEqualTo(testItem.orderQuantity)
        assertThat(capturedOrder.orderItems[0].optionId).isEqualTo(testItem.optionId)
        assertThat(capturedOrder.coffeeSubTotal).isEqualTo(cart.totalAmount)

        assertThat(result.memberId).isEqualTo(member.id)
        assertThat(result.coffeeSubTotal).isEqualTo(cart.totalAmount)
    }

    companion object {
        private val STANDARD_SHIPPING_AMOUNT = BigDecimal("25.00")
        private val FREE_SHIPPING_THRESHOLD = BigDecimal("50.00")
        private val STANDARD_SHIPPING_FEE = BigDecimal("5.99")
    }
}
