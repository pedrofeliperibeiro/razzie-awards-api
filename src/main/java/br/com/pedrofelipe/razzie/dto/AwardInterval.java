package br.com.pedrofelipe.razzie.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Um intervalo entre dois prêmios consecutivos de um mesmo produtor.
 */
@Schema(description = "Intervalo entre dois prêmios consecutivos de um mesmo produtor")
public record AwardInterval(
        @Schema(description = "Nome do produtor", example = "Joel Silver")
        String producer,
        @Schema(description = "Anos entre os dois prêmios consecutivos", example = "1")
        int interval,
        @Schema(description = "Ano do prêmio anterior", example = "1990")
        int previousWin,
        @Schema(description = "Ano do prêmio seguinte", example = "1991")
        int followingWin
) {
}
