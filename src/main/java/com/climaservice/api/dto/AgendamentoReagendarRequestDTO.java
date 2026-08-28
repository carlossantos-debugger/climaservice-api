package com.climaservice.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoReagendarRequestDTO(

        @NotNull(message = "A data/hora de início é obrigatória")
        LocalDateTime dataHoraInicio,

        @NotNull(message = "A data/hora de fim é obrigatória")
        LocalDateTime dataHoraFim

) {
}
