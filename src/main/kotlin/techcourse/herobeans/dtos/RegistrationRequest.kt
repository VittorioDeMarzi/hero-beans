package techcourse.herobeans.dtos

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegistrationRequest(
    @field:NotBlank(message = "Name must not be blank")
    @field:Pattern(
        regexp = "^[A-Za-zÀ-ÿ'\\-\\s]+$",
        message = "Invalid name format",
    )
    val name: String,
    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email must not be blank")
    val email: String,
    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    val password: String,
)
