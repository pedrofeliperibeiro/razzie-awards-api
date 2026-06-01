package br.com.pedrofelipe.razzie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração contra um conjunto de dados diferente (carregado de
 * {@code datasets/ties.csv}) em que dois produtores empatam no menor intervalo e
 * dois no maior. Prova que o endpoint retorna todos os produtores empatados, como o
 * contrato da API exige — não apenas um por lado.
 *
 * <ul>
 *   <li>Producer A: 2000, 2001 -> intervalo 1</li>
 *   <li>Producer B: 2010, 2011 -> intervalo 1</li>
 *   <li>Producer C: 1900, 1999 -> intervalo 99</li>
 *   <li>Producer D: 2000, 2099 -> intervalo 99</li>
 *   <li>Producer E: vitória única -> sem intervalo</li>
 *   <li>Producer F: não é vencedor -> ignorado</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        // Dataset alternativo...
        "app.movies.csv=datasets/ties.csv",
        // ...e um banco H2 próprio. O H2 em memória é único por nome no JVM inteiro,
        // então sem isso esses dados de empate vazariam para o banco que os outros
        // testes enxergam, deixando-os instáveis conforme a ordem de execução.
        "spring.datasource.url=jdbc:h2:mem:razzie-ties;DB_CLOSE_DELAY=-1"
})
class AwardIntervalTiesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsAllProducersTiedOnSmallestAndLargestInterval() throws Exception {
        mockMvc.perform(get("/producers/award-intervals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.min", hasSize(2)))
                .andExpect(jsonPath("$.min[0].producer").value("Producer A"))
                .andExpect(jsonPath("$.min[0].interval").value(1))
                .andExpect(jsonPath("$.min[1].producer").value("Producer B"))
                .andExpect(jsonPath("$.min[1].interval").value(1))
                .andExpect(jsonPath("$.max", hasSize(2)))
                .andExpect(jsonPath("$.max[0].producer").value("Producer C"))
                .andExpect(jsonPath("$.max[0].interval").value(99))
                .andExpect(jsonPath("$.max[1].producer").value("Producer D"))
                .andExpect(jsonPath("$.max[1].interval").value(99));
    }
}
