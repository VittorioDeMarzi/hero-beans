package techcourse.herobeans.controller

import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.dto.CoffeeDto
import techcourse.herobeans.service.CoffeeService

@RestController
@RequestMapping("/api/product")
class GuestController(
    private val coffeeService: CoffeeService,
) {
    @GetMapping()
    fun getAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "8") size: Int,
    ): ResponseEntity<Page<CoffeeDto>> {
        val products = coffeeService.getAllProducts(page, size)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/{productId}")
    fun getProductById(
        @PathVariable productId: Long,
    ): ResponseEntity<CoffeeDto> {
        val product = coffeeService.getProductById(productId)
        return ResponseEntity.ok(product)
    }
}
