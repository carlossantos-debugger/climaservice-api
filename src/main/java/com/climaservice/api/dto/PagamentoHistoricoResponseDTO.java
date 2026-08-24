package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusPagamento;

import java.time.LocalDateTime;

public record PagamentoHistoricoResponseDTO(

        Long id,
        StatusPagamento statusAnterior,
        StatusPagamento statusNovo,
        LocalDateTime dataAlteracao,
        Long usuarioId,
        String usuarioNome

) {
}