package techcourse.herobeans.event

import org.springframework.context.ApplicationEvent

data class UserRegisteredEvent(val userMail: String) : ApplicationEvent(userMail)
