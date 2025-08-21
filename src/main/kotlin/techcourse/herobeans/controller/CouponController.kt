package techcourse.herobeans.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.annotation.LoginMember
import techcourse.herobeans.dto.CouponResponse
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.mapper.CouponMapper.toResponse
import techcourse.herobeans.service.CouponService

@RestController
class CouponController(private val couponService: CouponService) {
    @GetMapping("api/me/coupons")
    fun getCoupons(
        @LoginMember member: MemberDto,
    ): ResponseEntity<List<CouponResponse>> {
        val coupons = couponService.getAllCouponsForUser(member.email)
        return ResponseEntity.ok(coupons.map { it.toResponse() })
    }
}
