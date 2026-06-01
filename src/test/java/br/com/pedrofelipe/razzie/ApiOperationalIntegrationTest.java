package br.com.pedrofelipe.razzie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração das pontas operacionais da API: erros conhecidos saindo no
 * formato ProblemDetail (RFC 7807) com os status certos — parte do nível 2 de
 * Richardson — e o health do Actuator no ar (a observabilidade que a app expõe).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiOperationalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unknownRouteReturnsNotFoundAsProblemDetail() throws Exception {
        mockMvc.perform(get("/producers/does-not-exist"))
                .andExpect(status().isNotFound())
                // erro conhecido do Spring sai como RFC 7807 de verdade, não whitelabel legado
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unsupportedMethodReturnsMethodNotAllowedAsProblemDetail() throws Exception {
        mockMvc.perform(post("/producers/award-intervals"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void actuatorHealthIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
