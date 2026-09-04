package com.climaservice.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PlanoManutencaoPreventivaRequestDTO(

        @NotNull(message = "O equipamento é obrigatório")
        Long equipamentoId,

        Long tecnicoPadraoId,

        @NotNull(message = "O intervalo em meses é obrigatório")
        @Positive(message = "O intervalo em meses deve ser maior que zero")
        Integer intervaloMeses,

        LocalDate proximaExecucao,

        @Size(max = 1000, message = "A observação deve possuir no máximo 1000 caracteres")
        String observacao

) {
}
