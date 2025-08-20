package techcourse.herobeans.controller

import jakarta.validation.Valid
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
import techcourse.herobeans.dto.FinalizePaymentResponse
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.CheckoutStartRequest
import techcourse.herobeans.dto.CheckoutStartResponse
import techcourse.herobeans.service.CheckoutService

// TODO: address process need
@RestController
@RequestMapping("/api/checkout")
@Validated
class CheckoutController(
    private val checkoutService: CheckoutService,
) {
    @PostMapping("/start")
    fun start(
        @LoginMember member: MemberDto,
        @RequestBody request: CheckoutStartRequest,
    ): ResponseEntity<CheckoutStartResponse> {
        val response = checkoutService.startCheckout(member, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/finalize")
    fun finalize(
        @LoginMember member: MemberDto,
        @Valid @RequestBody request: FinalizePaymentRequest,
    ): ResponseEntity<FinalizePaymentResponse> {
        val paymentResult = checkoutService.finalizeCheckout(member, request)
        return ResponseEntity.ok(paymentResult.response)
    }
}
