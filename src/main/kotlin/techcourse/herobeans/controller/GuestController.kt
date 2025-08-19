package techcourse.herobeans.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.dto.CoffeeDto
import techcourse.herobeans.dto.CoffeeFilterCriteria
import techcourse.herobeans.enums.BrewRecommendation
import techcourse.herobeans.enums.CoffeeSorting
import techcourse.herobeans.enums.OriginCountry
import techcourse.herobeans.enums.ProcessingMethod
import techcourse.herobeans.enums.RoastLevel
import techcourse.herobeans.service.CoffeeService

@Tag(name = "Catalog", description = "Public product catalog")
@RestController
@RequestMapping("/api/product")
class GuestController(
    private val coffeeService: CoffeeService,
) {
    @Operation(summary = "List all coffees")
    @GetMapping()
    fun getAll(
        // Pagination parameters
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "8") size: Int,
        // Filtering parameters
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) origin: OriginCountry?,
        @RequestParam(required = false) roastLevel: RoastLevel?,
        @RequestParam(required = false) brewRecommendation: Set<BrewRecommendation?>?,
        @RequestParam(required = false) processingMethod: ProcessingMethod?,
        @RequestParam(required = false, defaultValue = "false") availableOnly: Boolean,
        // Sorting parameters
        @RequestParam(defaultValue = "ID_ASC") sort: CoffeeSorting,
    ): ResponseEntity<Page<CoffeeDto>> {
        val filterCriteria =
            CoffeeFilterCriteria(
                name = name,
                originCountry = origin,
                roastLevel = roastLevel,
                brew = brewRecommendation,
                processingMethod = processingMethod,
                availableOnly = availableOnly,
            )
        val products = coffeeService.getAllProducts(page, size, filterCriteria, sort)
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
