package techcourse.herobeans

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping
    fun hello(): String {
        return "Hello world! This is @HeroBeans."
    }
}
