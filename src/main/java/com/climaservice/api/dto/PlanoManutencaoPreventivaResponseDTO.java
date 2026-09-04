package com.climaservice.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PlanoManutencaoPreventivaResponseDTO(

        Long id,

        Long equipamentoId,
        String equipamentoMarca,
        String equipamentoModelo,

        Long tecnicoPadraoId,
        String tecnicoPadraoNome,

        Integer intervaloMeses,
        LocalDate proximaExecucao,
        LocalDate ultimaExecucao,

        Boolean ativo,
        String observacao,

        LocalDateTime dataCriacao

) {
}
