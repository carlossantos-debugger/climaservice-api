package com.climaservice.api.dto;

import java.time.LocalDateTime;

public record OrdemServicoDiagnosticoHistoricoResponseDTO(

        Long id,

        String diagnosticoAnterior,

        String diagnosticoNovo,

        LocalDateTime dataAlteracao,

        Long usuarioId,

        String usuarioNome

) {
}