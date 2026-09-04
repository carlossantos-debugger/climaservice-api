package com.climaservice.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PlanoManutencaoPreventivaExecucaoResponseDTO(

        Long id,
        Long planoId,
        Long ordemServicoId,

        LocalDate dataReferencia,
        LocalDateTime dataExecucao,

        Long usuarioId,
        String usuarioNome

) {
}
