package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusOrdemServico;

import java.time.LocalDateTime;

public record OrdemServicoHistoricoResponseDTO(

        Long id,
        StatusOrdemServico statusAnterior,
        StatusOrdemServico statusNovo,
        LocalDateTime dataAlteracao,
        Long usuarioId,
        String usuarioNome

) {
}