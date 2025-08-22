package techcourse.herobeans.service

import mu.KotlinLogging
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

private val log = KotlinLogging.logger {}

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
            .also { resp -> log.info { "cart.viewed memberId=${member.id} items=${cart.items.size}" } }
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

        val saved =
            cartRepository.saveAndFlush(cart)
                .also { c ->
                    log.info { "cart.item.added memberId=${member.id} cartId=${c.id} optionId=$optionId totalItems=${c.items.size}" }
                }
        return saved.items.first { it.option.id == optionId }
    }

    fun removeProductFromCart(
        member: MemberDto,
        optionId: Long,
    ) {
        val cart = getOrCreateCart(member.id)
        cart.removeItem(optionId)
        log.info { "cart.item.removed memberId=${member.id} cartId=${cart.id} optionId=$optionId totalItems=${cart.items.size}" }
    }

    fun clearCart(memberId: Long) {
        val cart = getOrCreateCart(memberId)
        cart.clear()
        log.info { "cart.cleared memberId=$memberId cartId=${cart.id} items=0" }
    }

    fun getCartForOrder(memberId: Long): Cart {
        val cart =
            cartRepository.findByMemberId(memberId)
                ?: throw NotFoundException("Cannot find cart by memberId: $memberId")
        return cart
    }

    private fun getOrCreateCart(memberId: Long): Cart {
        cartRepository.findByMemberId(memberId)?.let { existing ->
            return existing.also { c -> log.info { "cart.loaded memberId=$memberId cartId=${c.id} items=${c.items.size}" } }
        }
        val member =
            memberRepository.findById(memberId)
                .orElseThrow { NotFoundException("Member not found: $memberId") }
        return cartRepository.save(Cart(member))
            .also { c -> log.info { "cart.created memberId=$memberId cartId=${c.id}" } }
    }
}
