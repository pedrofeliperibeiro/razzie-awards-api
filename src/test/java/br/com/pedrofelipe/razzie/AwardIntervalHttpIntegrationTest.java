package br.com.pedrofelipe.razzie;

import br.com.pedrofelipe.razzie.dto.AwardIntervalResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste sobre um servidor HTTP real ({@code RANDOM_PORT} + {@link TestRestTemplate}),
 * em vez do MockMvc.
 *
 * <p>Por que existe além dos testes de MockMvc: o MockMvc não executa o dispatch de
 * erro para {@code /error}, então ele não enxerga diferenças que só aparecem na pilha
 * de servlet de verdade (como o {@code Content-Type} de um erro). Este teste fecha
 * esse ponto cego batendo na porta real — tanto o caminho feliz quanto um erro
 * conhecido (405) saindo como {@code application/problem+json} (RFC 7807).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AwardIntervalHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void returnsExpectedIntervalsOverRealHttp() {
        ResponseEntity<AwardIntervalResult> response =
                restTemplate.getForEntity("/producers/award-intervals", AwardIntervalResult.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().min())
                .singleElement()
                .satisfies(min -> {
                    assertThat(min.producer()).isEqualTo("Joel Silver");
                    assertThat(min.interval()).isEqualTo(1);
                    assertThat(min.previousWin()).isEqualTo(1990);
                    assertThat(min.followingWin()).isEqualTo(1991);
                });
        assertThat(response.getBody().max())
                .singleElement()
                .satisfies(max -> {
                    assertThat(max.producer()).isEqualTo("Matthew Vaughn");
                    assertThat(max.interval()).isEqualTo(13);
                    assertThat(max.previousWin()).isEqualTo(2002);
                    assertThat(max.followingWin()).isEqualTo(2015);
                });
    }

    @Test
    void unsupportedMethodReturnsProblemJsonOverRealHttp() {
        ResponseEntity<String> response =
                restTemplate.postForEntity("/producers/award-intervals", null, String.class);

        // É exatamente o que o MockMvc não pega: o Content-Type do erro no servidor real.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).contains("\"status\":405");
    }
}
