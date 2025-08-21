package techcourse.herobeans.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.annotation.LoginMember
import techcourse.herobeans.dto.CartProductItem
import techcourse.herobeans.dto.CartProductResponse
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.entity.CartItem
import techcourse.herobeans.mapper.CartMapper
import techcourse.herobeans.service.CartService

@Tag(name = "Cart", description = "Member cart operations")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/member/cart")
class CartController(private val cartService: CartService) {
    @Operation(summary = "Get current cart")
    @GetMapping("")
    fun getCartItems(
        @LoginMember member: MemberDto,
    ): ResponseEntity<CartProductResponse> {
        val products = cartService.getCartProducts(member)
        return ResponseEntity.ok(products)
    }

    @Operation(summary = "Add option to cart (increments if exists)")
    @PostMapping("/{id}")
    fun addProduct(
        @LoginMember member: MemberDto,
        @PathVariable("id") optionId: Long,
    ): ResponseEntity<CartProductItem> {
        val item: CartItem = cartService.addProductToCart(member, optionId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CartMapper.toItem(item))
    }

    @Operation(summary = "Remove specific option from cart")
    @DeleteMapping("/{id}")
    fun removeProduct(
        @LoginMember member: MemberDto,
        @PathVariable("id") optionId: Long,
    ): ResponseEntity<Void> {
        cartService.removeProductFromCart(member, optionId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Clear the cart")
    @DeleteMapping("/clear")
    fun clearCart(
        @LoginMember member: MemberDto,
    ): ResponseEntity<Void> {
        cartService.clearCart(member.id)
        return ResponseEntity.noContent().build()
    }
}
