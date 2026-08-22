package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusOrcamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrcamentoResponseDTO(

        Long id,

        Long ordemServicoId,

        StatusOrcamento status,

        BigDecimal valorTotal,

        LocalDateTime dataCriacao,

        LocalDateTime dataEnvio,

        LocalDateTime dataResposta,

        String observacao

) {
}