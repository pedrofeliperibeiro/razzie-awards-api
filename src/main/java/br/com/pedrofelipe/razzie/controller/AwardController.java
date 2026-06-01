package br.com.pedrofelipe.razzie.controller;

import br.com.pedrofelipe.razzie.dto.AwardIntervalResult;
import br.com.pedrofelipe.razzie.service.AwardIntervalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/producers")
@Tag(name = "Producers", description = "Intervalos de prêmios dos produtores")
public class AwardController {

    private final AwardIntervalService awardIntervalService;

    public AwardController(AwardIntervalService awardIntervalService) {
        this.awardIntervalService = awardIntervalService;
    }

    /**
     * Retorna os produtores com o menor e o maior intervalo entre dois prêmios
     * consecutivos.
     *
     * @return HTTP 200 com o payload {@code {min, max}}.
     */
    @Operation(
            summary = "Intervalos de prêmios por produtor",
            description = "Retorna o(s) produtor(es) com o menor e o maior intervalo entre dois "
                    + "prêmios consecutivos. Ambos os lados são listas, então empates vêm completos.")
    @ApiResponse(responseCode = "200", description = "Intervalos calculados com sucesso")
    @GetMapping("/award-intervals")
    public AwardIntervalResult getAwardIntervals() {
        return awardIntervalService.getAwardIntervals();
    }
}
