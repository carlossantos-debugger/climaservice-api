package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusAgendamento;

import java.time.LocalDateTime;

public record AgendamentoResponseDTO(

        Long id,

        Long ordemServicoId,

        Long tecnicoId,
        String tecnicoNome,

        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,

        StatusAgendamento status,
        String observacao,

        LocalDateTime dataCriacao

) {
}
