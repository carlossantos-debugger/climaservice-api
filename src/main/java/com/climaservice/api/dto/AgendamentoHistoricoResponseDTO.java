package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusAgendamento;

import java.time.LocalDateTime;

public record AgendamentoHistoricoResponseDTO(

        Long id,
        StatusAgendamento statusAnterior,
        StatusAgendamento statusNovo,
        LocalDateTime dataAlteracao,
        Long usuarioId,
        String usuarioNome

) {
}
