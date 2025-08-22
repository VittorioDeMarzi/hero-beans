package techcourse.herobeans.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.annotation.LoginMember
import techcourse.herobeans.dto.CheckoutStartRequest
import techcourse.herobeans.dto.CheckoutStartResponse
import techcourse.herobeans.dto.FinalizePaymentRequest
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.PaymentResult
import techcourse.herobeans.service.CheckoutService

// TODO: address process need
@Tag(name = "Checkout", description = "Checkout and payment flow")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/checkout")
@Validated
class CheckoutController(
    private val checkoutService: CheckoutService,
) {
    @Operation(summary = "Start checkout")
    @PostMapping("/start")
    fun start(
        @LoginMember member: MemberDto,
        @RequestBody request: CheckoutStartRequest,
    ): ResponseEntity<CheckoutStartResponse> {
        val response = checkoutService.startCheckout(member, request)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Finalize payment")
    @PostMapping("/finalize")
    fun finalize(
        @LoginMember member: MemberDto,
        @RequestBody request: FinalizePaymentRequest,
    ): ResponseEntity<PaymentResult> {
        val paymentResult = checkoutService.finalizeCheckout(member, request)
        return ResponseEntity.ok(paymentResult)
    }
}
