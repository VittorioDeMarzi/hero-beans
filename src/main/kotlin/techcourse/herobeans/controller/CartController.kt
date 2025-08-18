package techcourse.herobeans.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.annotation.LoginMember
import techcourse.herobeans.dto.CartProductResponse
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.MessageResponseDto
import techcourse.herobeans.service.CartService
import java.net.URI

@RestController
@RequestMapping("/api/member/cart")
class CartController(private val cartService: CartService) {
    @GetMapping("")
    fun getCartItems(
        @LoginMember member: MemberDto,
    ): ResponseEntity<CartProductResponse> {
        val products = cartService.getCartProducts(member)
        return ResponseEntity.ok(products)
    }

    @PostMapping("/{id}")
    fun addProduct(
        @LoginMember member: MemberDto,
        @PathVariable("id") optionId: Long,
    ): ResponseEntity<MessageResponseDto> {
        val id = cartService.addProductToCart(member, optionId)
        return ResponseEntity
            .created(URI.create("/api/member/cart/$id"))
            .body(MessageResponseDto("Product added to cart"))
    }

    @DeleteMapping("/{id}")
    fun removeProduct(
        @LoginMember member: MemberDto,
        @PathVariable("id") optionId: Long,
    ): ResponseEntity<Void> {
        cartService.removeProductFromCart(member, optionId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/clear")
    fun clearCart(
        @LoginMember member: MemberDto,
    ): ResponseEntity<Void> {
        cartService.clearCart(member)
        return ResponseEntity.noContent().build()
    }
}
