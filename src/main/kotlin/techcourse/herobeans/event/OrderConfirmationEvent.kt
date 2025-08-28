package techcourse.herobeans.event

import org.springframework.context.ApplicationEvent
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.entity.Order

data class OrderConfirmationEvent(val order: Order, val member: MemberDto) : ApplicationEvent(order)
