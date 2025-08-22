package techcourse.herobeans.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import techcourse.herobeans.service.CouponService
import techcourse.herobeans.service.EmailService

@Component
class WelcomeCouponListener(private val couponService: CouponService, private val emailService: EmailService) {
    @EventListener
    fun handleUserRegisteredEvent(event: UserRegisteredEvent) {
        val coupon = couponService.createWelcomeCoupon(event.userMail)
        emailService.sendRegistrationEmail(event.userMail, event.name, coupon.code)
        // TODO Log the creation of the welcome coupon
    }
}
