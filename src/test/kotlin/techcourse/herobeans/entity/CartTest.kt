package techcourse.herobeans.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@DataJpaTest
@ActiveProfiles("test")
class CartTest {
    private fun member(): Member = mock()

    private fun option(
        id: Long,
        price: BigDecimal,
    ): PackageOption {
        val opt = mock<PackageOption>()
        whenever(opt.id).thenReturn(id)
        whenever(opt.price).thenReturn(price)
        return opt
    }

    private fun cartItem(
        cart: Cart? = null,
        option: PackageOption,
        qty: Int,
    ) = CartItem(cart, option, qty)

    @Test
    fun `addOrIncrement - adds new item`() {
        val cart = Cart(member = member())
        val opt = option(1L, BigDecimal("10.00"))

        cart.addOrIncrement(cartItem(option = opt, qty = 2))

        assertThat(cart.items).hasSize(1)
        assertThat(cart.items.first().quantity).isEqualTo(2)
        assertThat(cart.totalAmount).isEqualByComparingTo("20.00")
    }

    @Test
    fun `addOrIncrement - increments existing item`() {
        val cart = Cart(member = member())
        val opt = option(1L, BigDecimal("5.00"))

        cart.addOrIncrement(cartItem(option = opt, qty = 1))
        cart.addOrIncrement(cartItem(option = opt, qty = 3))

        assertThat(cart.items).hasSize(1)
        assertThat(cart.items.first().quantity).isEqualTo(4)
        assertThat(cart.totalAmount).isEqualByComparingTo("20.00")
    }

    @Test
    fun `addOrIncrement - fails if quantity is zero`() {
        val cart = Cart(member = member())
        val opt = option(1L, BigDecimal("10.00"))

        assertThrows<IllegalArgumentException> { cart.addOrIncrement(cartItem(option = opt, qty = 0)) }
    }

    @Test
    fun `removeItem - removes correct item`() {
        val cart = Cart(member = member())
        val opt1 = option(1L, BigDecimal("3.00"))
        val opt2 = option(2L, BigDecimal("4.00"))

        cart.addOrIncrement(cartItem(option = opt1, qty = 1))
        cart.addOrIncrement(cartItem(option = opt2, qty = 2))

        cart.removeItem(1L)

        assertThat(cart.items).hasSize(1)
        assertThat(cart.items.first().option.id).isEqualTo(2L)
        assertThat(cart.totalAmount).isEqualByComparingTo("8.00")
    }

    @Test
    fun `clear - removes all items`() {
        val cart = Cart(member = member())
        val opt1 = option(1L, BigDecimal("1.00"))
        val opt2 = option(2L, BigDecimal("2.00"))

        cart.addOrIncrement(cartItem(option = opt1, qty = 1))
        cart.addOrIncrement(cartItem(option = opt2, qty = 2))

        cart.clear()

        assertThat(cart.items).isEmpty()
        assertThat(cart.totalAmount).isEqualByComparingTo("0.00")
    }

    @Test
    fun `CartItem - priceSnapshot keeps initial price`() {
        val opt = option(1L, BigDecimal("10.00"))
        val item = cartItem(option = opt, qty = 1)
        assertThat(item.priceSnapshot).isEqualByComparingTo("10.00")

        whenever(opt.price).thenReturn(BigDecimal("12.00"))
        assertThat(item.priceSnapshot).isEqualByComparingTo("10.00")
    }
}
