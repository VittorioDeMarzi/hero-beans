package techcourse.herobeans.controller

import ecommerce.annotation.AdminOnly
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
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
import techcourse.herobeans.dto.PackageOptionRequest
import techcourse.herobeans.service.CoffeeService

private val log = KotlinLogging.logger {}

@Tag(name = "Admin: Coffee", description = "Admin operations for managing coffees")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/coffees")
class AdminCoffeeController(
    private val coffeeService: CoffeeService,
) {
    @Operation(summary = "Create a coffee")
    @PostMapping
    fun createProduct(
        @Valid @RequestBody coffee: CoffeeRequest,
        @AdminOnly member: MemberDto,
    ): ResponseEntity<CoffeeDto> {
        log.info { "api.admin.coffee.create requested adminId=${member.id}" }
        val newProduct = coffeeService.createCoffee(coffee)
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct)
    }

    @Operation(summary = "Delete a coffee")
    @DeleteMapping("/{id}")
    fun deleteProduct(
        @PathVariable id: Long,
        @AdminOnly member: MemberDto,
    ): ResponseEntity<Unit> {
        log.info { "api.admin.coffee.delete requested adminId=${member.id} coffeeId=$id" }
        coffeeService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Patch a coffee")
    @PatchMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody coffee: CoffeePatchRequest,
        @AdminOnly member: MemberDto,
    ): ResponseEntity<CoffeeDto> {
        log.info { "api.admin.coffee.update requested adminId=${member.id} coffeeId=$id" }
        val updatedProduct = coffeeService.updateProduct(id, coffee)
        return ResponseEntity.ok(updatedProduct)
    }

    @Operation(summary = "Add package option to coffee")
    @PostMapping("/add/option/{id}")
    fun addOptionToProduct(
        @PathVariable id: Long,
        @AdminOnly member: MemberDto,
        @Valid @RequestBody option: PackageOptionRequest,
    ): ResponseEntity<CoffeeDto> {
        log.info { "api.admin.coffee.option.add requested adminId=${member.id} coffeeId=$id" }
        val updatedProduct = coffeeService.addOptionToCoffee(id, option)
        return ResponseEntity.ok(updatedProduct)
    }
}
