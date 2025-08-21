package techcourse.herobeans.sliceTest

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import techcourse.herobeans.exception.GlobalExceptionHandler

@ActiveProfiles("test")
class GlobalExceptionHandlerWebMvcTest {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        val jackson = MappingJackson2HttpMessageConverter()

        val builder =
            MockMvcBuilders
                .standaloneSetup(FakeExceptionController())
                .setControllerAdvice(GlobalExceptionHandler())
                .setMessageConverters(jackson)
                .setValidator(validator)

        mockMvc = builder.build()
    }

    @Test
    fun `404 NotFound returns ErrorMessageModel`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/dummy/not-found/42"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Not Found"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Entity not found: 42"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.path").value("/dummy/not-found/42"))
    }

    @Test
    fun `400 bad JSON`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/dummy/bad-json")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(("not a json")),
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Bad Request"))
    }

    @Test
    fun `400 validation body`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/dummy/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content("""{"amount":0}"""),
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value("must be greater than or equal to 1"))
    }

    @Test
    fun `403 forbidden`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/dummy/forbidden"))
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(403))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("No access"))
    }

    @Test
    fun `401 unauthorized`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/dummy/unauthorized"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `409 conflict`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/dummy/conflict"))
            .andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `500 generic masked message`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/dummy/generic"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(500))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Internal server error"))
    }

    @Test
    fun `propagates incoming correlation id`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/dummy/not-found/1").header("X-Correlation-Id", "test-cid-123"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
