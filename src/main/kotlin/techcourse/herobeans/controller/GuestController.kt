package techcourse.herobeans.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.dto.CoffeeDto
import techcourse.herobeans.service.CoffeeService

@Tag(name = "Catalog", description = "Public product catalog")
@RestController
@RequestMapping("/api/product")
class GuestController(
    private val coffeeService: CoffeeService,
) {
    @Operation(summary = "List all coffees")
    @GetMapping()
    fun getAll(): ResponseEntity<List<CoffeeDto>> {
        val products = coffeeService.getAllProducts()
        return ResponseEntity.ok(products)
    }

    @Operation(summary = "Get coffee by id")
    @GetMapping("/{productId}")
    fun getProductById(
        @PathVariable productId: Long,
    ): ResponseEntity<CoffeeDto> {
        val product = coffeeService.getProductById(productId)
        return ResponseEntity.ok(product)
    }
}
