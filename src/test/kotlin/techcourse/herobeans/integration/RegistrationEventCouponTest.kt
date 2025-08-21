package techcourse.herobeans.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import techcourse.herobeans.dto.RegistrationRequest
import techcourse.herobeans.repository.CouponJpaRepository

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationEventCouponTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var couponRepository: CouponJpaRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should create welcome coupon after registration`() {
        val request = RegistrationRequest(email = "test@example.com", password = "pass12345678", name = "someone")

        mockMvc.perform(
            post("/api/members/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isCreated)

        val coupons = couponRepository.findAllByUserMail("test@example.com")
        assert(coupons.isNotEmpty()) { "Welcome coupon should be created" }
    }
}
