package br.com.pedrofelipe.razzie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prova, de ponta a ponta, que a vírgula de Oxford é tratada num filme vencedor — o
 * parsing de produtores é o ponto de destaque do projeto, então vale um teste que
 * cobre exatamente isso, e não só de forma implícita.
 *
 * <p>No dataset {@code datasets/oxford.csv}:
 * <ul>
 *   <li>2000 (vencedor): "Ana Lima, Bruno Costa, and Carlos Dias"</li>
 *   <li>2005 (vencedor): "Carlos Dias"</li>
 * </ul>
 * Se a vírgula de Oxford fosse mal tratada, o último produtor sairia como
 * "and Carlos Dias", não casaria com o de 2005 e nenhum intervalo apareceria. Como o
 * resultado esperado é Carlos Dias com intervalo 5 (2000 -> 2005), o teste garante
 * que o nome foi extraído limpo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.movies.csv=datasets/oxford.csv",
        "spring.datasource.url=jdbc:h2:mem:razzie-oxford;DB_CLOSE_DELAY=-1"
})
class ProducerOxfordCommaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void extractsLastProducerFromOxfordCommaWinner() throws Exception {
        mockMvc.perform(get("/producers/award-intervals"))
                .andExpect(status().isOk())
                // o produtor que aparece depois do ", and " foi extraído limpo
                .andExpect(jsonPath("$.min[0].producer").value("Carlos Dias"))
                .andExpect(jsonPath("$.min[0].interval").value(5))
                .andExpect(jsonPath("$.min[0].previousWin").value(2000))
                .andExpect(jsonPath("$.min[0].followingWin").value(2005))
                // e não existe nenhum produtor "fantasma" com o "and" grudado
                .andExpect(jsonPath("$.max[0].producer").value("Carlos Dias"));
    }
}
