package techcourse.herobeans.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.annotation.LoginMember
import techcourse.herobeans.dto.CouponResponse
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.mapper.CouponMapper.toResponse
import techcourse.herobeans.service.CouponService

private val log = KotlinLogging.logger {}

@Tag(name = "Coupon", description = "Member coupon operations")
@SecurityRequirement(name = "bearerAuth")
@RestController
class CouponController(private val couponService: CouponService) {
    @Operation(summary = "Get all coupons for current member")
    @GetMapping("api/me/coupons")
    fun getCoupons(
        @LoginMember member: MemberDto,
    ): ResponseEntity<List<CouponResponse>> {
        log.info { "api.coupon.get requested memberId=${member.id} email=${member.email}" }
        val coupons = couponService.getAllCouponsForUser(member.email)
        log.info { "api.coupon.get.success memberId=${member.id} email=${member.email} count=${coupons.size}" }
        return ResponseEntity.ok(coupons.map { it.toResponse() })
    }
}
