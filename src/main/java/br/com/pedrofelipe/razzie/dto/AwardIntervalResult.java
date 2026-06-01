package br.com.pedrofelipe.razzie.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Resposta da API: produtores com o menor e o maior intervalo entre dois prêmios
 * consecutivos. Ambos os lados são listas para suportar empates.
 */
@Schema(description = "Produtores com o menor e o maior intervalo entre dois prêmios consecutivos")
public record AwardIntervalResult(
        @Schema(description = "Produtor(es) com o menor intervalo — todos os empatados")
        List<AwardInterval> min,
        @Schema(description = "Produtor(es) com o maior intervalo — todos os empatados")
        List<AwardInterval> max
) {
}
