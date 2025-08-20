package techcourse.herobeans.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.dto.CartProductResponse
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.entity.Cart
import techcourse.herobeans.entity.CartItem
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.mapper.CartMapper
import techcourse.herobeans.repository.CartJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository
import techcourse.herobeans.repository.PackageOptionJpaRepository

@Service
@Transactional
class CartService(
    private val cartRepository: CartJpaRepository,
    private val memberRepository: MemberJpaRepository,
    private val optionRepository: PackageOptionJpaRepository,
) {
    fun getCartProducts(member: MemberDto): CartProductResponse {
        val cart = getOrCreateCart(member.id)
        return CartMapper.toResponse(cart)
    }

    fun addProductToCart(
        member: MemberDto,
        optionId: Long,
    ): CartItem {
        val cart = getOrCreateCart(requireNotNull(member.id))
        val option =
            optionRepository.findById(optionId)
                .orElseThrow { NotFoundException("Package option not found: $optionId") }

        val newItem = CartItem(cart = cart, option = option, quantity = 1)
        cart.addOrIncrement(newItem)
        val saved = cartRepository.save(cart)
        return saved.items.first { it.option.id == optionId }
    }

    fun removeProductFromCart(
        member: MemberDto,
        optionId: Long,
    ) {
        val cart = getOrCreateCart(member.id)
        cart.removeItem(optionId)
    }

    fun clearCart(member: MemberDto) {
        val cart = getOrCreateCart(member.id)
        cart.clear()
    }

    fun getCartForOrder(memberId: Long): Cart {
        val cart =
            cartRepository.findByMemberId(memberId)
                ?: throw NotFoundException("Cannot find cart by memberId: $memberId")

        // TODO: validate if you need
        return cart
    }

    private fun getOrCreateCart(memberId: Long): Cart {
        cartRepository.findByMemberId(memberId)?.let { return it }
        val member =
            memberRepository.findById(memberId)
                .orElseThrow { NotFoundException("Member not found: $memberId") }
        return cartRepository.save(Cart(member))
    }
}
