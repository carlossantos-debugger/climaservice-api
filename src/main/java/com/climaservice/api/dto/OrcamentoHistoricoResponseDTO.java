package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusOrcamento;

import java.time.LocalDateTime;

public record OrcamentoHistoricoResponseDTO(

        Long id,
        StatusOrcamento statusAnterior,
        StatusOrcamento statusNovo,
        LocalDateTime dataAlteracao,
        Long usuarioId,
        String usuarioNome

) {
}