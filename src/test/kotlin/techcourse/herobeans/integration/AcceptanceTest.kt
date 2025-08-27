package techcourse.herobeans.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AcceptanceTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun cors() {
        mockMvc.perform(
            MockMvcRequestBuilders.options("/api/coffees")
                .header(HttpHeaders.ORIGIN, "https://www.herobeans.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"),
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://www.herobeans.com"))
            .andExpect(
                MockMvcResultMatchers.header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHOD_NAMES),
            )
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION))
            .andDo(MockMvcResultHandlers.print())
    }

    companion object {
        private const val ALLOWED_METHOD_NAMES = "GET,POST,HEAD,OPTIONS,DELETE,PUT,PATCH"
    }
}
