package com.climaservice.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PlanoManutencaoPreventivaAtualizarRequestDTO(

        Long tecnicoPadraoId,

        @NotNull(message = "O intervalo em meses é obrigatório")
        @Positive(message = "O intervalo em meses deve ser maior que zero")
        Integer intervaloMeses,

        @NotNull(message = "A próxima execução é obrigatória")
        LocalDate proximaExecucao,

        @Size(max = 1000, message = "A observação deve possuir no máximo 1000 caracteres")
        String observacao

) {
}
