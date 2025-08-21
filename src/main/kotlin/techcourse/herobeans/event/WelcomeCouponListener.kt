package techcourse.herobeans.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import techcourse.herobeans.service.CouponService

@Component
class WelcomeCouponListener(private val couponService: CouponService) {
    @EventListener
    fun handleUserRegisteredEvent(event: UserRegisteredEvent) {
        couponService.createWelcomeCoupon(event.userMail)
    }
}
