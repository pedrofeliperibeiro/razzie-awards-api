package br.com.pedrofelipe.razzie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração do endpoint de intervalos de prêmios.
 *
 * <p>Sobe o contexto completo da aplicação: o CSV é carregado no banco H2 embarcado
 * na inicialização e as asserções abaixo verificam que os valores retornados por
 * {@code GET /producers/award-intervals} batem com os dados fornecidos no desafio.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AwardIntervalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsProducersWithSmallestAndLargestIntervalFromProvidedData() throws Exception {
        mockMvc.perform(get("/producers/award-intervals"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                // menor intervalo: Joel Silver, 1990 -> 1991
                .andExpect(jsonPath("$.min", hasSize(1)))
                .andExpect(jsonPath("$.min[0].producer").value("Joel Silver"))
                .andExpect(jsonPath("$.min[0].interval").value(1))
                .andExpect(jsonPath("$.min[0].previousWin").value(1990))
                .andExpect(jsonPath("$.min[0].followingWin").value(1991))
                // maior intervalo: Matthew Vaughn, 2002 -> 2015
                .andExpect(jsonPath("$.max", hasSize(1)))
                .andExpect(jsonPath("$.max[0].producer").value("Matthew Vaughn"))
                .andExpect(jsonPath("$.max[0].interval").value(13))
                .andExpect(jsonPath("$.max[0].previousWin").value(2002))
                .andExpect(jsonPath("$.max[0].followingWin").value(2015));
    }

    @Test
    void responseAlwaysExposesMinAndMaxArrays() throws Exception {
        mockMvc.perform(get("/producers/award-intervals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.min").isArray())
                .andExpect(jsonPath("$.max").isArray());
    }
}
