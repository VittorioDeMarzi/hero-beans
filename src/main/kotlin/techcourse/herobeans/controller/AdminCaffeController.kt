package techcourse.herobeans.controller

import ecommerce.annotation.AdminOnly
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.dto.CoffeeDto
import techcourse.herobeans.dto.CoffeePatchRequest
import techcourse.herobeans.dto.CoffeeRequest
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.service.CoffeeService

@RestController
@RequestMapping("/api/admin/coffees")
class AdminCaffeController(
    private val coffeeService: CoffeeService,
) {
    @PostMapping
    fun createProduct(
        @Valid @RequestBody coffee: CoffeeRequest,
        @AdminOnly member: MemberDto,
    ): ResponseEntity<CoffeeDto> {
        val newProduct = coffeeService.createCoffee(coffee)
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(
        @PathVariable id: Long,
        @AdminOnly member: MemberDto,
    ): ResponseEntity<Void> {
        coffeeService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody coffee: CoffeePatchRequest,
        @AdminOnly member: MemberDto,
    ): ResponseEntity<CoffeeDto> {
        val updatedProduct = coffeeService.updateProduct(id, coffee)
        return ResponseEntity.ok(updatedProduct)
    }
}
