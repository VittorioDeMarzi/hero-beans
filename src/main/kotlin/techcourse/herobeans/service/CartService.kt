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
        val cart = getOrCreateCart(requireNotNull(member.id))
        return CartMapper.toResponse(cart)
    }

    fun addProductToCart(
        member: MemberDto,
        optionId: Long,
    ): Long {
        val cart = getOrCreateCart(requireNotNull(member.id))
        val option =
            optionRepository.findById(optionId)
                .orElseThrow { IllegalArgumentException("Package option not found: $optionId") }

        val newItem =
            CartItem(
                cart = cart,
                option = option,
                quantity = 1,
            )
        cart.addOrIncrement(newItem)

        val saved = cartRepository.saveAndFlush(cart)
        val updatedItem = saved.items.first { it.option.id == optionId }
        return updatedItem.id
    }

    fun removeProductFromCart(
        member: MemberDto,
        optionId: Long,
    ) {
        val cart = getOrCreateCart(requireNotNull(member.id))
        cart.removeItem(optionId)
        cartRepository.save(cart)
    }

    fun clearCart(member: MemberDto) {
        val cart = getOrCreateCart(requireNotNull(member.id))
        cart.clear()
        cartRepository.save(cart)
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
                .orElseThrow { IllegalArgumentException("Member not found: $memberId") }
        return cartRepository.save(Cart(member))
    }
}
